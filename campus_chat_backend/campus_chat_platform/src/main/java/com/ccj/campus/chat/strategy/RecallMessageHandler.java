package com.ccj.campus.chat.strategy;

import com.ccj.campus.chat.cache.MessageCache;
import com.ccj.campus.chat.cache.UserInfoCache;
import com.ccj.campus.chat.dao.MessageDao;
import com.ccj.campus.chat.dto.RecallMessageDto;
import com.ccj.campus.chat.entity.MessageExtra;
import com.ccj.campus.chat.entity.Messages;
import com.ccj.campus.chat.entity.Users;
import com.ccj.campus.chat.enums.MessageTypeEnum;
import com.ccj.campus.chat.service.MessagesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @Author ccj
 * @Date 2024-06-07 00:17
 * @Description 处理撤回消息
 */
@Component
@RequiredArgsConstructor
public class RecallMessageHandler extends AbstractMessageHandler<RecallMessageDto> {

    private final UserInfoCache userInfoCache;
    private final MessagesService messagesService;
    private final MessageDao messageDao;
    private final MessageCache messageCache;

    @Override
    public Object showMsg(Messages msg) {
        Users user = userInfoCache.get(msg.getFromUid());
        return user.getFullName() + " 撤回了一条消息";
    }

    @Override
    public Object showReplyMsg(Messages msg) {
        return "原消息已被撤回";
    }

    @Override
    public String showContactMsg(Long uid, Messages msg) {
        String prefix = messagesService.getRecallMessageUserName(uid, msg);
        return prefix + "撤回了一条消息";
    }

    @Override
    public MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.RECALL;
    }

    @Override
    public void saveMessage(Messages message, RecallMessageDto messageContent) {
        //不需要做任何操作
    }

    @Transactional(rollbackFor = Exception.class)
    public void recall(Long recallMsgId) {
        MessageExtra extra = new MessageExtra();
        extra.setRecallMessage(new RecallMessageDto(recallMsgId, LocalDateTime.now()));
        messageDao.updateMessageFieldsById(recallMsgId, null, null, null, MessageTypeEnum.RECALL.getType(), null, extra);

        Messages message = messageCache.getMessage(recallMsgId);
        if (message != null) {
            message.setType(MessageTypeEnum.RECALL.getType());
            message.setExtra(extra);
            messageCache.setTemporaryMessage(message);
        }
    }
}
