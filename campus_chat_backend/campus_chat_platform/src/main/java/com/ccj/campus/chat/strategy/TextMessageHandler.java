package com.ccj.campus.chat.strategy;

import cn.hutool.core.collection.CollectionUtil;
import com.ccj.campus.chat.cache.*;
import com.ccj.campus.chat.dao.MessageDao;
import com.ccj.campus.chat.dto.TextMessageReq;
import com.ccj.campus.chat.dto.TextMessageResp;
import com.ccj.campus.chat.entity.*;
import com.ccj.campus.chat.enums.DeleteStatusEnum;
import com.ccj.campus.chat.enums.MessageTypeEnum;
import com.ccj.campus.chat.enums.RoomTypeEnum;
import com.ccj.campus.chat.enums.YesOrNo;
import com.ccj.campus.chat.imservice.service.MessageHandlerFactory;
import com.ccj.campus.chat.imservice.service.adapter.MessageAdapter;
import com.ccj.campus.chat.service.ContactService;
import com.ccj.campus.chat.service.MessagesService;
import com.ccj.campus.chat.utils.AssertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @Author ccj
 * @Date 2024-05-09 21:04
 * @Description 处理文本消息
 */
@Component
@RequiredArgsConstructor
public class TextMessageHandler extends AbstractMessageHandler<TextMessageReq> {

    private final MessagesService messagesService;
    private final GroupManagerCache groupManagerCache;
    private final RoomGroupCache roomGroupCache;
    private final UserInfoCache userInfoCache;
    private final MessageCache messageCache;
    private final RoomCache roomCache;
    private final ContactService contactService;
    private final MessageDao messageDao;

    @Override
    public Object showMsg(Messages msg) {
        TextMessageResp resp = new TextMessageResp();
        resp.setContent(msg.getContent());
        resp.setAtUidList(Optional.ofNullable(msg.getExtra()).map(MessageExtra::getAtUidList).orElse(null));
        //回复消息

        Optional<Messages> replyOptional = Optional.ofNullable(msg.getReplyMsgId()).map(messagesService::getMessageById)
                .filter(message -> DeleteStatusEnum.NOT_DELETED.getStatus().equals(message.getStatus()));

        if (replyOptional.isPresent()) {
            Messages replyMessage = replyOptional.get();
            TextMessageResp.ReplyMessage replyVO = new TextMessageResp.ReplyMessage();

            replyVO.setId(replyMessage.getId());
            replyVO.setUid(replyMessage.getFromUid());
            replyVO.setType(replyMessage.getType());

            AbstractMessageHandler<?> handler = MessageHandlerFactory.getMessageHandlerNonNull(replyMessage.getType());
            replyVO.setBody(handler.showReplyMsg(replyMessage));

            Users fromUser = userInfoCache.get(replyMessage.getFromUid());
            replyVO.setUsername(fromUser.getFullName());

            replyVO.setGapCount(msg.getGapCount());

            boolean canCallback = msg.getGapCount() != null && msg.getGapCount() <= MessageAdapter.MAX_REPLY_MESSAGE_GAP_COUNT;
            replyVO.setCanCallback(YesOrNo.parse(canCallback));

            resp.setReplyMessage(replyVO);
        }

        return resp;
    }

    @Override
    public Object showReplyMsg(Messages msg) {
        return msg.getContent();
    }

    @Override
    public String showContactMsg(Long uid, Messages msg) {
        String prefix = messagesService.getRecallMessageUserName(uid, msg);
        return prefix + msg.getContent();
    }

    @Override
    public MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.TEXT;
    }

    @Override
    public void saveMessage(Messages message, TextMessageReq textMessageReq) {
        MessageExtra extra = Optional.ofNullable(message.getExtra()).orElse(new MessageExtra());
        Messages update = new Messages();
        update.setId(message.getId());
        //todo ...后期可以考虑做敏感词过滤，也可以不做，跟微信一样，发言较为自由
        update.setContent(textMessageReq.getContent());

        //如果有回复消息
        if (textMessageReq.getReplyMessageId() != null) {
            Integer gapCount = messageDao.getGapCount(message.getRoomId(), textMessageReq.getReplyMessageId(), message.getId());
            update.setGapCount(gapCount);
            update.setReplyMsgId(textMessageReq.getReplyMessageId());
        }

        //只有群聊才能at
        Room room = roomCache.get(message.getRoomId());
        if (room != null && RoomTypeEnum.GROUP_ROOM.getType().equals(room.getType())) {
            //at群成员
            if (CollectionUtil.isNotEmpty(textMessageReq.getAtUidList())) {
                extra.setAtUidList(textMessageReq.getAtUidList());
            }

            //at所有人
            if (Boolean.TRUE.equals(textMessageReq.getAtAllUser())) {
                extra.setAtAllUser(true);
            }
        }

        update.setExtra(extra);
        messageDao.updateMessageFieldsById(update.getId(), update.getContent(), update.getReplyMsgId(), update.getGapCount(), null, null, update.getExtra());

        //缓存到redis, 在consumer的时候就可以直接缓存拿
        message.setExtra(update.getExtra());
        message.setGapCount(update.getGapCount());
        message.setReplyMsgId(update.getReplyMsgId());
        message.setContent(update.getContent());

        messageCache.setTemporaryMessage(message);
    }


    @Override
    protected void check(Long uid, TextMessageReq messageContent, Long roomId) {
        //校验回复的内容
        if (messageContent.getReplyMessageId() != null) {
            Messages message = messageDao.getById(messageContent.getReplyMessageId());
            AssertUtil.isNotEmpty(message, "回复的消息不存在");
            AssertUtil.equal(message.getRoomId(), roomId, "回复的消息不在当前聊天室");
        }

        Room room = roomCache.get(roomId);

        //at群成员
        if (RoomTypeEnum.isGroupRoom(room.getType()) && CollectionUtil.isNotEmpty(messageContent.getAtUidList())) {
            AssertUtil.isFalse(messageContent.getAtUidList().contains(uid), "不能艾特自己");
            //前端可能可能会传递重复的at
            List<Long> distinctAtUidList = messageContent.getAtUidList().stream().distinct().toList();
            AssertUtil.isTrue(distinctAtUidList.size() == messageContent.getAtUidList().size(), "不能重复艾特用户");
        }

        //at所有人
        if (RoomTypeEnum.isGroupRoom(room.getType()) && messageContent.getAtAllUser()) {
            AssertUtil.isFalse(messageContent.getAtUidList() != null && messageContent.getAtUidList().size() > 0, "不能同时艾特所有人和指定用户");
            //是否有权限@所有人
            //拿到具体的群id
            RoomGroup roomGroup = roomGroupCache.get(roomId);
            Set<String> UidListOfManagerAndOwner = groupManagerCache.get(roomGroup.getId());
            AssertUtil.isTrue(UidListOfManagerAndOwner.contains(uid.toString()), "你还没有权限艾特所有人");
        }
    }
}
