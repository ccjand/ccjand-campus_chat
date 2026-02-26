package com.ccj.campus.chat.service;


import com.ccj.campus.chat.dto.CursorPageBaseResp;
import com.ccj.campus.chat.entity.Messages;
import com.ccj.campus.chat.entity.Room;
import com.ccj.campus.chat.imservice.domain.dto.PullMessagePage;
import com.ccj.campus.chat.imservice.domain.vo.req.*;
import com.ccj.campus.chat.imservice.domain.vo.resp.ChatContactResp;
import com.ccj.campus.chat.imservice.domain.vo.resp.ChatMessageResp;
import com.ccj.campus.chat.imservice.domain.vo.resp.MsgReadInfoResp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author ccj
 * @Date 2024-05-07 15:52
 * @Description
 */
public interface ChatService {

    /**
     * 发送消息
     *
     * @param uid            用户uid
     * @param chatMessageReq 消息请求
     * @param rectification  是否需要消息整流
     * @return 返回消息id
     */
    Long sendMessage(Long uid, ChatMessageReq chatMessageReq, boolean rectification);


    /**
     * 包装单条消息
     */
    ChatMessageResp getChatMessageResp(Messages message, Long receiveUid);


    /**
     * 包装消息列表
     */
    List<ChatMessageResp> getChatMessageRespBatch(List<Messages> messageList, Long receiveUid);


    /**
     * 一个响应包含单条消息
     */
    ChatMessageResp getMessageResp(Long messageId, Long uid);


    /**
     * 获取房间的消息列表
     *
     * @param uid            当前登录用户
     * @param messagePageReq 消息列表需要的请求参数
     * @return 返回游标分页后的消息列表
     */
    CursorPageBaseResp<ChatMessageResp> getMessageList(Long uid, ChatMessagePageReq messagePageReq);


    /**
     * 撤回消息
     */
    void recallMessage(Long uid, RecallMessageReq recallMessageReq);

    void deleteMessage(Long uid, RecallMessageReq deleteMessageReq);


    /**
     * 有人发送｜撤回消息需要更新房间的最后一条消息id和消息更新时间
     */
    void refreshRoomAndContact(Room room, Long lastMessageId, LocalDateTime activeTime, Set<Long> groupMemberUidSet);


    /**
     * 保存并缓存消息（默认1分钟）
     */
    Messages saveAndCacheMessage(Messages message);


    /**
     * 保存并缓存消息
     */
    Messages saveAndCacheMessage(Messages message, long time, TimeUnit timeUnit);

    /**
     * 更新并删除缓存
     */
    void updateAndDeleteCache(Long messageId, String content);

    /**
     * 消息阅读时间线上报
     */
    void readMsg(Long uid, ReadContactMsgReq readMsgReq);

    /**
     * 消息已读未读数
     */
    List<MsgReadInfoResp> getMsgReadInfo(Long uid, MsgReadInfoReq msgReadInfoReq);

    /**
     * 游标分页查询已读或未读的用户列表
     */
    CursorPageBaseResp<Long> getMsgReadOrUnreadList(Long uid, ChatMessageReadReq chatMessageReadReq);

    /**
     * 获取最近的会话列表（默认30个）
     */
    List<ChatContactResp> getRecentContactList(Long uid);

    /**
     * 批次全量拉取消息
     *
     * @param uid
     * @param req
     * @return
     */
    PullMessagePage pullMessageList(Long uid, PullMessageListReq req);


    /**
     * 拉取的是  上一次pull的最大msgId < msgId < 上一次push的最大Id【虽然可能会和前端本地库的少部分重复，但是绝对不会漏查】
     */
    PullMessagePage pullIntervalMessageList(Long uid, PullIntervalMessageReq req);
}
