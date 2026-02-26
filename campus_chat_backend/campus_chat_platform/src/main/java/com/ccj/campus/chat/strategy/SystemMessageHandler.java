package com.ccj.campus.chat.strategy;

import com.ccj.campus.chat.cache.MessageCache;
import com.ccj.campus.chat.dao.MessageDao;
import com.ccj.campus.chat.entity.Messages;
import com.ccj.campus.chat.enums.MessageTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @Author ccj
 * @Date 2024-05-11 17:55
 * @Description
 */
@Component
@RequiredArgsConstructor
public class SystemMessageHandler extends AbstractMessageHandler<String> {

    private final MessageDao messageDao;
    private final MessageCache messageCache;

    @Override
    public Object showMsg(Messages msg) {
        return msg.getContent();
    }

    @Override
    public Object showReplyMsg(Messages msg) {
        return msg.getContent();
    }

    @Override
    public String showContactMsg(Long uid, Messages msg) {
        return msg.getContent();
    }

    @Override
    public MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.SYSTEM;
    }

    @Override
    public void saveMessage(Messages message, String systemMessage) {
        Messages update = new Messages();
        update.setId(message.getId());
        update.setContent(systemMessage);
        messageDao.updateById(update);

        //缓存到redis, 在consumer的时候就可以直接缓存拿
        message.setContent(systemMessage);
        messageCache.setTemporaryMessage(message);
    }
}
