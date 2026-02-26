package com.ccj.campus.chat.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.ccj.campus.chat.cache.*;
import com.ccj.campus.chat.config.ThreadPoolConfig;
import com.ccj.campus.chat.dao.ContactDao;
import com.ccj.campus.chat.dao.MessageDao;
import com.ccj.campus.chat.dto.*;
import com.ccj.campus.chat.entity.*;
import com.ccj.campus.chat.enums.DeleteStatusEnum;
import com.ccj.campus.chat.enums.MessageTypeEnum;
import com.ccj.campus.chat.enums.MsgReadOrUnreadType;
import com.ccj.campus.chat.enums.RoomTypeEnum;
import com.ccj.campus.chat.exception.BusinessException;
import com.ccj.campus.chat.exception.CommonErrorEnum;
import com.ccj.campus.chat.imservice.domain.dto.PullMessagePage;
import com.ccj.campus.chat.imservice.domain.vo.req.*;
import com.ccj.campus.chat.imservice.domain.vo.resp.ChatContactResp;
import com.ccj.campus.chat.imservice.domain.vo.resp.ChatMessageResp;
import com.ccj.campus.chat.imservice.domain.vo.resp.MsgReadInfoResp;
import com.ccj.campus.chat.imservice.domain.vo.resp.WSMsgRecall;
import com.ccj.campus.chat.imservice.service.WebSocketService;
import com.ccj.campus.chat.imservice.service.MessageHandlerFactory;
import com.ccj.campus.chat.imservice.service.adapter.WebSocketAdapter;
import com.ccj.campus.chat.imservice.service.adapter.MessageAdapter;
import com.ccj.campus.chat.redissonlock.annotation.RedissonLock;
import com.ccj.campus.chat.service.ChatService;
import com.ccj.campus.chat.strategy.AbstractMessageHandler;
import com.ccj.campus.chat.strategy.RecallMessageHandler;
import com.ccj.campus.chat.utils.AssertUtil;
import com.ccj.campus.chat.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-05-07 15:52
 * @Description
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    public static final int NOT_EXIST_ROOM_TYPE = -1;
    private final UserFriendCache userFriendCache;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RoomCache roomCache;
    private final RoomGroupCache roomGroupCache;
    private final GroupMemberCache groupMemberCache;
    private final MessageCache messageCache;
    private final ContactDao contactDao;
    private final MessageDao messageDao;
    private final UserInfoCache userInfoCache;
    private final RecallMessageHandler recallMessageHandler;
    private final RoomFriendCache roomFriendCache;
    private final WebSocketService webSocketService;

    @Autowired
    @Qualifier(ThreadPoolConfig.CAMPUS_CHAT_EXECUTOR)
    private ThreadPoolTaskExecutor executor;


    /**
     * 除了发送消息的接口在调用的时候会进行参数校验, 其他调用者自行校验参数,
     * 并且如果不是单｜群聊消息的话, roomType、 msgSeq、random、timestamp的值可以任意,
     * 因为不需要进行消息的整流【例如好友申请消息、系统消息等是不需要整流的】
     *
     * @param uid            发送方uid
     * @param chatMessageReq 消息请求
     * @return 返回消息的递增id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(Long uid, ChatMessageReq chatMessageReq, boolean rectification) {
        //生成秒级别的时间戳，用于后续窗口处理的同秒级包内消息整流【单｜群聊的整流手段不一致】
        if (chatMessageReq.getTimestamp() == null) {
            chatMessageReq.setTimestamp(DateUtil.getTimestampInSecond());
        }
        //使用多线程的房间多重检验
        SendMessageCheckDto messageCheckDto = check(uid, chatMessageReq, rectification);
        if (messageCheckDto.getFindMessageId() != null) {
            //这是前端失败重试导致的重复发送的消息, 直接返回, 不再处理
            return messageCheckDto.getFindMessageId();
        }

        //或者消息处理器，比如文本，语音，视频有不同的处理...【是用来工厂 + 适配器 + 模版方法】
        AbstractMessageHandler<?> messageHandler =
                MessageHandlerFactory.getMessageHandlerNonNull(chatMessageReq.getMsgType());
        Long messageId = messageHandler.checkAndSaveMessage(uid, chatMessageReq);

        //发送消息事件
        SendMessageEvent sendMessageEvent =
                new SendMessageEvent(this, messageCheckDto.getRoomType(), messageId,
                        chatMessageReq.getMsgSeq(), chatMessageReq.getTimestamp(),
                        rectification, chatMessageReq.getRandom(), chatMessageReq.getRoomId());
        applicationEventPublisher.publishEvent(sendMessageEvent);
        return messageId;
    }

    @TransactionalEventListener(classes = SendMessageEvent.class, phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleSendMessageEvent(SendMessageEvent event) {
        if (event == null || event.getRoomId() == null || event.getMessageId() == null) {
            return;
        }

        Room room = roomCache.get(event.getRoomId());
        if (room == null) {
            return;
        }

        Set<Long> memberUids = new HashSet<>();
        if (RoomTypeEnum.isGroupRoom(room.getType())) {
            memberUids.addAll(groupMemberCache.getGroupMember(room.getId()));
        } else if (RoomTypeEnum.isSingleRoom(room.getType())) {
            RoomFriend roomFriend = roomFriendCache.get(room.getId());
            if (roomFriend != null) {
                if (roomFriend.getUid1() != null) memberUids.add(roomFriend.getUid1());
                if (roomFriend.getUid2() != null) memberUids.add(roomFriend.getUid2());
            }
        }

        if (memberUids.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        refreshRoomAndContact(room, event.getMessageId(), now, memberUids);

        if (event.getTimestampInSeconds() != null && event.getRoomMsgSeq() != null && event.getRandom() != null) {
            messageCache.saveRepeatMessage(room.getId(), event.getTimestampInSeconds(), event.getRoomMsgSeq(), event.getRandom(), event.getMessageId());
        }
    }

    @Override
    public ChatMessageResp getChatMessageResp(Messages message, Long receiveUid) {
        return getChatMessageRespBatch(Collections.singletonList(message), receiveUid).get(0);
    }

    @Override
    public List<ChatMessageResp> getChatMessageRespBatch(List<Messages> messageList, Long uid) {
        if (CollectionUtil.isEmpty(messageList)) {
            return new ArrayList<>();
        }
        List<ChatMessageResp> resp = MessageAdapter.buildChatMesasgeRespList(messageList, uid);
        List<Long> fromUidList = messageList.stream().map(Messages::getFromUid).collect(Collectors.toList());

        fromUidList.remove(Users.SYSTEM_ID);
        Map<Long, Users> map = userInfoCache.getBatch(fromUidList);

        resp = resp.parallelStream().peek(k -> {
            ChatMessageResp.UserInfo fromUser = k.getFromUser();

            if (Users.SYSTEM_ID.equals(fromUser.getUid())) {
                fromUser.setName("群聊系统消息");
                fromUser.setAvatar(null);
            } else {
                String fullName = Optional.ofNullable(map.get(fromUser.getUid())).map(Users::getFullName).orElse(null);
                fromUser.setName(fullName);
                fromUser.setAvatar(map.get(fromUser.getUid()).getAvatar());
            }
        }).collect(Collectors.toList());

        return resp;
    }

    @Override
    public ChatMessageResp getMessageResp(Long messageId, Long uid) {
        Messages message = messageCache.getMessage(messageId);
        return this.getChatMessageResp(message, uid);
    }

    @Override
    public CursorPageBaseResp<ChatMessageResp> getMessageList(Long uid, ChatMessagePageReq req) {
        //会话的最大消息id
        Long lastMessageId = getLastMessageId(uid, req.getRoomId());
        CursorPageBaseResp<Messages> cursorPage = messageDao.getCursorPage(req, lastMessageId);
        if (cursorPage.isEmpty()) {
            return CursorPageBaseResp.empty();
        }

        List<ChatMessageResp> list = getChatMessageRespBatch(cursorPage.getList(), uid);

        return CursorPageBaseResp.init(cursorPage, list);
    }

    @Override
    public List<ChatContactResp> getRecentContactList(Long uid) {
        CursorPageBaseResp<Contact> page = contactDao.contactList(uid, new CursorPageBaseReq(30, null));
        List<Contact> contactList = page != null ? page.getList() : null;
        if (CollectionUtil.isEmpty(contactList)) {
            return Collections.emptyList();
        }

        List<Long> roomIdList = contactList.stream()
                .map(Contact::getRoomId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Room> roomMap = roomCache.getBatch(roomIdList);
        Map<Long, RoomFriend> roomFriendMap = roomFriendCache.getBatch(roomIdList);
        Map<Long, RoomGroup> roomGroupMap = roomGroupCache.getBatch(roomIdList);

        List<Long> peerUidList = roomIdList.stream()
                .map(roomId -> {
                    RoomFriend roomFriend = roomFriendMap.get(roomId);
                    if (roomFriend == null) return null;
                    if (!Objects.equals(roomFriend.getStatus(), DeleteStatusEnum.NOT_DELETED.getStatus())) return null;
                    Long uid1 = roomFriend.getUid1();
                    Long uid2 = roomFriend.getUid2();
                    if (uid1 == null || uid2 == null) return null;
                    if (!Objects.equals(uid1, uid) && !Objects.equals(uid2, uid)) return null;
                    return Objects.equals(uid1, uid) ? uid2 : uid1;
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Users> userMap = userInfoCache.getBatch(peerUidList);

        Set<Long> lastMsgIdSet = new HashSet<>();
        for (Contact contact : contactList) {
            if (contact == null || contact.getRoomId() == null) continue;
            Long msgId = contact.getLastMsgId();
            if (msgId == null) {
                Room room = roomMap.get(contact.getRoomId());
                msgId = room != null ? room.getLastMsgId() : null;
            }
            if (msgId != null) lastMsgIdSet.add(msgId);
        }

        Map<Long, Messages> lastMessageMap = new HashMap<>();
        for (Long msgId : lastMsgIdSet) {
            Messages message = messageCache.getMessage(msgId);
            if (message != null) lastMessageMap.put(msgId, message);
        }

        return contactList.stream()
                .filter(Objects::nonNull)
                .map(contact -> {
                    Long roomId = contact.getRoomId();
                    if (roomId == null) return null;
                    Room room = roomMap.get(roomId);
                    if (room == null || room.getType() == null) return null;

                    ChatContactResp resp = new ChatContactResp();
                    resp.setId(roomId);
                    resp.setRoomId(roomId);

                    Messages lastMsg = null;
                    Long msgId = contact.getLastMsgId();
                    if (msgId == null) msgId = room.getLastMsgId();
                    if (msgId != null) lastMsg = lastMessageMap.get(msgId);

                    if (lastMsg != null) {
                        resp.setSummary(lastMsg.getContent());
                        resp.setTimestamp(lastMsg.getCreateTime());
                    } else {
                        resp.setSummary("");
                        resp.setTimestamp(Optional.ofNullable(contact.getActiveTime()).orElse(room.getActiveTime()));
                    }

                    resp.setUnreadCount(messageDao.getUnReadMessageCount(roomId, contact.getReadTime()));

                    if (RoomTypeEnum.isGroupRoom(room.getType())) {
                        resp.setMessageType("group");
                        RoomGroup roomGroup = roomGroupMap.get(roomId);
                        if (roomGroup == null) return null;
                        if (!Objects.equals(roomGroup.getDeleteStatus(), DeleteStatusEnum.NOT_DELETED.getStatus())) return null;
                        Set<Long> members = groupMemberCache.getGroupMember(roomId);
                        if (members == null || !members.contains(uid) || members.size() < 2) return null;
                        resp.setName(roomGroup.getName());
                        resp.setAvatar(roomGroup.getAvatar());
                        return resp;
                    }

                    if (!RoomTypeEnum.isSingleRoom(room.getType())) return null;
                    resp.setMessageType("single");
                    RoomFriend roomFriend = roomFriendMap.get(roomId);
                    if (roomFriend == null) return null;
                    if (!Objects.equals(roomFriend.getStatus(), DeleteStatusEnum.NOT_DELETED.getStatus())) return null;
                    if (roomFriend.getUid1() == null || roomFriend.getUid2() == null) return null;
                    if (!Objects.equals(roomFriend.getUid1(), uid) && !Objects.equals(roomFriend.getUid2(), uid)) return null;
                    Long peerUid = Objects.equals(roomFriend.getUid1(), uid) ? roomFriend.getUid2() : roomFriend.getUid1();
                    if (peerUid == null) return null;
                    UserFriend friend = userFriendCache.getFriend(uid, peerUid);
                    if (friend == null || !Objects.equals(friend.getDeleteStatus(), DeleteStatusEnum.NOT_DELETED.getStatus())) return null;
                    resp.setPeerUid(peerUid);

                    Users peer = peerUid != null ? userMap.get(peerUid) : null;
                    if (peer == null) return null;
                    resp.setName(peer.getFullName());
                    resp.setAvatar(peer.getAvatar());
                    return resp;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public PullMessagePage pullMessageList(Long uid, PullMessageListReq req) {
        List<Messages> pullBatch = messageDao.pullMessageList(req);

        if (pullBatch.isEmpty()) {
            return PullMessagePage.empty();
        }

        List<ChatMessageResp> list = getChatMessageRespBatch(pullBatch, uid);

        return PullMessagePage.init(list);
    }

    @Override
    public PullMessagePage pullIntervalMessageList(Long uid, PullIntervalMessageReq req) {
        List<Messages> pullBatch = messageDao.pullIntervalMessageList(req);

        if (pullBatch.isEmpty()) {
            return PullMessagePage.empty();
        }

        List<ChatMessageResp> list = getChatMessageRespBatch(pullBatch, uid);

        return PullMessagePage.init(list);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(Long uid, RecallMessageReq recallMessageReq) {
        //校验撤回操作的合法性
        checkRecallMessage(uid, recallMessageReq);

        //撤回消息
        recallMessageHandler.recall(recallMessageReq.getMessageId());

        //发布撤回消息事件
        RecallChatMessageDto recallMessage = new RecallChatMessageDto(uid, recallMessageReq.getRoomId(), recallMessageReq.getMessageId());
        applicationEventPublisher.publishEvent(new RecallMessageEvent(this, recallMessage));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long uid, RecallMessageReq deleteMessageReq) {
        Messages message = messageCache.getMessage(deleteMessageReq.getMessageId());
        AssertUtil.isNotEmpty(message, "消息不存在");
        AssertUtil.equal(message.getFromUid(), uid, "只能删除自己发送的消息");
        AssertUtil.equal(message.getStatus(), DeleteStatusEnum.NOT_DELETED.getStatus(), "消息已被删除,无法操作");
        AssertUtil.equal(message.getRoomId(), deleteMessageReq.getRoomId(), "只能删除当前会话的消息");

        messageDao.updateMessageFieldsById(message.getId(), null, null, null, null, DeleteStatusEnum.DELETED.getStatus(), null);
        messageCache.deleteCache(message.getId());
    }

    @TransactionalEventListener(classes = RecallMessageEvent.class, phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleRecallMessageEvent(RecallMessageEvent event) {
        RecallChatMessageDto dto = event != null ? event.getRecallMessage() : null;
        if (dto == null || dto.getRoomId() == null || dto.getRecallMessageId() == null) {
            return;
        }

        Room room = roomCache.get(dto.getRoomId());
        if (room == null) {
            return;
        }

        Set<Long> memberUids = new HashSet<>();
        if (RoomTypeEnum.isGroupRoom(room.getType())) {
            memberUids.addAll(groupMemberCache.getGroupMember(room.getId()));
        } else if (RoomTypeEnum.isSingleRoom(room.getType())) {
            RoomFriend roomFriend = roomFriendCache.get(room.getId());
            if (roomFriend != null) {
                if (roomFriend.getUid1() != null) memberUids.add(roomFriend.getUid1());
                if (roomFriend.getUid2() != null) memberUids.add(roomFriend.getUid2());
            }
        }

        if (memberUids.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        refreshRoomAndContact(room, dto.getRecallMessageId(), now, memberUids);

        String name = null;
        if (dto.getFromUid() != null) {
            Users user = userInfoCache.get(dto.getFromUid());
            if (user != null) {
                name = user.getFullName();
            }
        }

        WSMsgRecall recall = new WSMsgRecall();
        recall.setRoomId(dto.getRoomId());
        recall.setMessageId(dto.getRecallMessageId());
        recall.setOperatorUid(dto.getFromUid());
        recall.setText((name != null && !name.isBlank() ? name + " " : "") + "撤回了一条消息");

        memberUids.forEach(targetUid -> webSocketService.sendToOnlineUser(targetUid, WebSocketAdapter.buildRecallResp(recall)));
    }

    @Override
    public void refreshRoomAndContact(Room room, Long lastMessageId, LocalDateTime activeTime, Set<Long> groupMemberUidSet) {
        //刷新房间里消息的最新更新时间和最新消息id
        room.setLastMsgId(lastMessageId);
        room.setActiveTime(activeTime);

        //刷新并重新设置房间的缓存
        refreshChatRoomCache(room, lastMessageId);

        //更新所有群成员的会话时间【如果不存在会话就创建】
        groupMemberUidSet.remove(Users.SYSTEM_ID);
        contactDao.createOrUpdateContact(groupMemberUidSet, room.getId(), activeTime, lastMessageId);
    }

    @Override
    public Messages saveAndCacheMessage(Messages message) {
        return saveAndCacheMessage(message, 1, TimeUnit.MINUTES);
    }

    @Override
    public Messages saveAndCacheMessage(Messages message, long time, TimeUnit timeUnit) {
        messageDao.save(message);
        messageCache.setTemporaryMessage(message, time, timeUnit);
        return message;
    }

    @Override
    public void updateAndDeleteCache(Long messageId, String content) {
        messageDao.updateContent(messageId, content);
        messageCache.deleteCache(messageId);
    }


    /**
     * 延长聊天室的缓存时间
     */
    private void refreshChatRoomCache(Room room, Long messageId) {
        //更新房间和会话的最新消息id
        roomCache.refreshRoom(room, messageId);
        //延长群聊房间的时间
        roomGroupCache.refresh(room.getId(), null);
    }

    private void checkRecallMessage(Long uid, RecallMessageReq recallMessageReq) {
        Messages message = messageCache.getMessage(recallMessageReq.getMessageId());
        AssertUtil.isNotEmpty(message, "消息不存在");
        AssertUtil.equal(message.getFromUid(), uid, "不能撤回别人的消息");
        AssertUtil.equal(message.getStatus(), DeleteStatusEnum.NOT_DELETED.getStatus(), "消息已被删除,无法操作");
        AssertUtil.equal(message.getRoomId(), recallMessageReq.getRoomId(), "只能撤销当前会话的消息");
        AssertUtil.notEqual(message.getType(), MessageTypeEnum.RECALL.getType(), "消息已被撤回,请勿重复操作");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime afterTowMinute = message.getCreateTime().plusMinutes(2L);
        AssertUtil.isTrue(afterTowMinute.compareTo(now) > 0, "消息已超过2分钟,无法撤回");

        //如果是群消息, 校验自己是否被提出群,被踢出群就不能操作了
        //如果是私聊消息, 不受限制, 即使自己被删除好友了,依旧不妨碍自己在2分钟内撤回自己的消息,只是对方收不到你撤回消息的信息了
        RoomGroup roomGroup = roomGroupCache.get(message.getRoomId());
        if (roomGroup != null) {
            AssertUtil.equal(roomGroup.getDeleteStatus(), DeleteStatusEnum.NOT_DELETED.getStatus(), "群已解散,无法撤回消息");
            boolean hasUser = groupMemberCache.hasUser(roomGroup.getRoomId(), uid);
            AssertUtil.isTrue(hasUser, "您已被踢出群,无法撤回消息");
        }

    }

    private Long getLastMessageId(Long uid, Long roomId) {
        Room room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "房间不存在");

        Contact contact = contactDao.getContact(uid, roomId);
        AssertUtil.isNotEmpty(contact, "会话不存在");

        return contact.getLastMsgId();
    }


    private SendMessageCheckDto check(Long uid, ChatMessageReq chatMessageReq, boolean rectification) {
        CompletableFuture<Room> roomCacheTask = CompletableFuture.supplyAsync(() -> rectification ? roomCache.get(chatMessageReq.getRoomId()) :
                Room.builder().type(NOT_EXIST_ROOM_TYPE).build(), executor);
        CompletableFuture<RoomFriend> roomFriendTask = CompletableFuture.supplyAsync(() -> roomFriendCache.get(chatMessageReq.getRoomId()), executor);
        CompletableFuture<RoomGroup> roomGroupTask = CompletableFuture.supplyAsync(() -> roomGroupCache.get(chatMessageReq.getRoomId()), executor);
        CompletableFuture<Set<Long>> groupMembersTask = CompletableFuture.supplyAsync(() -> {
            return groupMemberCache.getGroupMember(chatMessageReq.getRoomId());
        }, executor);
        CompletableFuture<Long> findMessageTask = CompletableFuture.supplyAsync(() -> messageCache.getRepeatMessage(chatMessageReq.getRoomId(), chatMessageReq.getTimestamp(),
                chatMessageReq.getMsgSeq(), chatMessageReq.getRandom()), executor);

        CompletableFuture<SendMessageCheckDto> allTasksFuture = CompletableFuture.allOf(roomCacheTask, roomFriendTask, roomGroupTask, groupMembersTask)
                .exceptionally(ex -> {
                    log.error("位于com.ccj.chat.moon.user.service.impl.ChatServiceImpl.check()的并行任务异常, 原因:{}", ex.getMessage(), ex);
                    throw new BusinessException(CommonErrorEnum.SYSTEM_ERROR);
                }).thenApplyAsync(res -> {
                    try {
                        Room room = roomCacheTask.join();
                        RoomFriend roomFriend = roomFriendTask.join();
                        RoomGroup roomGroup = roomGroupTask.join();
                        Set<Long> groupMembers = groupMembersTask.join();
                        Long findMsgId = findMessageTask.join();
                        AssertUtil.isNotEmpty(room, CommonErrorEnum.ROOM_NOT_EXIST);
                        if (room.getType().equals(RoomTypeEnum.SINGLE_ROOM.getType())) {
                            //单聊
                            AssertUtil.isNotEmpty(roomFriend, CommonErrorEnum.ROOM_FRIEND_NOT_EXIST);
                            AssertUtil.isTrue(DeleteStatusEnum.NOT_DELETED.getStatus().equals(roomFriend.getStatus()), CommonErrorEnum.NOT_FRIEND);
                            AssertUtil.isTrue(roomFriend.getUid1().equals(uid) || roomFriend.getUid2().equals(uid), CommonErrorEnum.INVALID_OPERATION);
                            AssertUtil.isNotEmpty(chatMessageReq.getMsgSeq(), "单聊必须携带msgSeq");
                        } else if (room.getType().equals(RoomTypeEnum.GROUP_ROOM.getType())) {
                            //群聊
                            //AssertUtil.isTrue(YesOrNo.judge(roomGroup.getEnableRobot()), "群聊机器人已关闭,无法发送消息");
                            AssertUtil.isTrue(roomGroup.getDeleteStatus().equals(DeleteStatusEnum.NOT_DELETED.getStatus()), CommonErrorEnum.ROOM_GROUP_NOT_EXIST);
                            AssertUtil.isTrue(groupMembers.contains(uid), "你还不是该群成员");
                        }
                        return new SendMessageCheckDto(findMsgId, room.getType());
                    } catch (Exception ex) {
                        log.error("位于com.ccj.chat.moon.user.service.impl.ChatServiceImpl.check中的thenAcceptAsync并行任务异常, 原因:{}", ex.getMessage(), ex);
                        throw ex;
                    }
                });

        //阻塞主线程,等待结果响应给前端
        try {
            return allTasksFuture.join();
        } catch (CompletionException e) {
            log.error("异常:{}", e.getMessage(), e);
            Throwable cause = e.getCause();
            if (cause instanceof BusinessException) {
                // 如果是自定义的BusinessException，直接抛出，保持原错误信息
                throw (BusinessException) cause;
            } else {
                log.error("系统异常:{}", e.getMessage(), e);
                throw new BusinessException(CommonErrorEnum.SYSTEM_ERROR);
            }
        }

    }


    @Override
    @RedissonLock(key = "'readMsg_' + #uid + ':' + #readMsgReq.roomId") //避免重复创建会话
    public void readMsg(Long uid, ReadContactMsgReq readMsgReq) {
        LocalDateTime now = LocalDateTime.now();
        //查处用户的会话
        Contact contact = contactDao.getContact(uid, readMsgReq.getRoomId());
        if (contact != null) {
            Contact update = new Contact();
            update.setId(contact.getId());
            update.setReadTime(now);
            update.setUpdateTime(now);
            contactDao.updateById(update);
        } else {
            Contact insert = new Contact();
            insert.setUid(uid);
            insert.setRoomId(readMsgReq.getRoomId());
            insert.setReadTime(now);
            insert.setActiveTime(now);
            insert.setCreateTime(now);
            insert.setUpdateTime(now);
            contactDao.save(insert);
        }
    }

    @Override
    public List<MsgReadInfoResp> getMsgReadInfo(Long uid, MsgReadInfoReq msgReadInfoReq) {
        List<Messages> messages = messageDao.listByIds(msgReadInfoReq.getMsgIds());
        messages.forEach(msg -> {
            //只能查看自己发送的消息
            AssertUtil.equal(msg.getFromUid(), uid, "只能查看自己发送的消息");
        });

        Map<Long, List<Messages>> msgMap = messages.stream().collect(Collectors.groupingBy(Messages::getRoomId));
        //必须是同一个会话下的消息, 不能跨会话查看
        AssertUtil.equal(msgMap.size(), 1, "只能查看同一个会话下的消息");

        //会话总人数
        Long roomId = msgMap.keySet().iterator().next();
        Integer totalCount = contactDao.getTotalCount(roomId);
        //key: 房间号  value: 已读消息数
        List<Long> messageIdList = msgMap.get(roomId).stream().map(Messages::getId).collect(Collectors.toList());
        Map<Long, Integer> readOrUnreadMap = messageDao.getMsgReadCount(roomId, uid, messageIdList);

        return messages.stream().map(msg -> {
            MsgReadInfoResp resp = new MsgReadInfoResp();
            resp.setMsgId(msg.getId());
//            Integer readCount = contactDao.getReadCount(msg);
            Integer readCount = readOrUnreadMap.getOrDefault(msg.getId(), 0);
            resp.setReadCount(readCount);
            resp.setUnReadCount(totalCount - readCount - 1);
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    public CursorPageBaseResp<Long> getMsgReadOrUnreadList(Long uid, ChatMessageReadReq chatMessageReadReq) {
        //校验消息
        Messages message = messageDao.getById(chatMessageReadReq.getMessageId());
        AssertUtil.isNotEmpty(message, "消息不存在");
        AssertUtil.equal(message.getFromUid(), uid, "只能查看自己发送的消息");

        CursorPageBaseResp<Contact> page;

        if (MsgReadOrUnreadType.isRead(chatMessageReadReq.getSearchType())) {
            //查询消息已读列表
            page = contactDao.readPage(message, chatMessageReadReq, uid);
        } else {
            //查询消息未读列表
            page = contactDao.unreadPage(message, chatMessageReadReq, uid);
        }

        List<Long> list = page.getList().stream().map(Contact::getUid).collect(Collectors.toList());
        return CursorPageBaseResp.init(page, list);
    }
}
