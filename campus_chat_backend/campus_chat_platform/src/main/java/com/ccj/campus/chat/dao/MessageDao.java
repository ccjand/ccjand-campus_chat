package com.ccj.campus.chat.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccj.campus.chat.dto.CursorPageBaseResp;
import com.ccj.campus.chat.dto.MapResultHandler;
import com.ccj.campus.chat.entity.MessageExtra;
import com.ccj.campus.chat.entity.Messages;
import com.ccj.campus.chat.enums.DeleteStatusEnum;
import com.ccj.campus.chat.imservice.domain.vo.req.ChatMessagePageReq;
import com.ccj.campus.chat.imservice.domain.vo.req.PullIntervalMessageReq;
import com.ccj.campus.chat.imservice.domain.vo.req.PullMessageListReq;
import com.ccj.campus.chat.mapper.MessagesMapper;
import com.ccj.campus.chat.util.JsonUtils;
import com.ccj.campus.chat.utils.CursorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 消息表 服务实现类
 * </p>
 *
 * @author CCJ
 * @since 2024-05-06
 */
@Service
@RequiredArgsConstructor
public class MessageDao extends ServiceImpl<MessagesMapper, Messages> {

    private final MessagesMapper messageMapper;

    public Integer getGapCount(Long roomId, Long fromMsgId, Long toMsgId) {
        return lambdaQuery().eq(Messages::getRoomId, roomId)
                .gt(Messages::getId, fromMsgId)
                .lt(Messages::getId, toMsgId)
                .count();
    }

    public CursorPageBaseResp<Messages> getCursorPage(ChatMessagePageReq req, Long lastMessageId) {
        return CursorUtils.getCursorPageFromMysql(this, req,
                wrapper -> wrapper.eq(Messages::getRoomId, req.getRoomId())
                        .eq(Messages::getStatus, DeleteStatusEnum.NOT_DELETED.getStatus()),
//                        .ne(Message::getType, MessageTypeEnum.SYSTEM.getType()), //不等于导致不走索引
                Messages::getId);

    }

    public Integer getUnReadMessageCount(Long roomId, LocalDateTime readTime) {
        return lambdaQuery().eq(Messages::getRoomId, roomId)
                .eq(Messages::getStatus, DeleteStatusEnum.NOT_DELETED.getStatus())
                .gt(Objects.nonNull(readTime), Messages::getCreateTime, readTime)
                .count();
    }

    public void updateContent(Long messageId, String responseStatus) {
        lambdaUpdate().eq(Messages::getId, messageId)
                .set(Messages::getContent, responseStatus)
                .update();
    }

    public void updateMessageFieldsById(Long id, String content, Long replyMsgId, Integer gapCount, Integer type, Integer status, MessageExtra extra) {
        String extraJson = extra == null ? null : JsonUtils.toStr(extra);
        messageMapper.updateMessageFieldsById(id, content, replyMsgId, gapCount, type, status, extraJson);
    }

    public Map<Long, Integer> getMsgReadCount(Long roomId, Long uid, List<Long> messageIdList) {
        MapResultHandler<Long, Integer> resultHandler = new MapResultHandler<>();
        messageMapper.getMsgReadCount(roomId, uid, messageIdList, resultHandler);
        return resultHandler.getMappedResults();
    }

    public List<Messages> getOfflineMessagePage(Long uid, PullMessageListReq req) {
//        return messageMapper.getOfflineMessagePage(uid, req.getBatchSize(), req.getLastMessageTime());
        return null;
    }

    public List<Messages> pullMessageList(PullMessageListReq req) {
        return messageMapper.pullMessageList(req.getRoomIdLastMsgIdMap(), req.getBatchSize());
    }

    public List<Messages> pullIntervalMessageList(PullIntervalMessageReq req) {
        return messageMapper.pullIntervalMessageList(req.getRoomId(), req.getLastPullMessageId(), req.getLastPushMessageId(), req.getBatchSize());
    }

}
