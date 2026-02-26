package com.ccj.campus.chat.strategy;

import com.ccj.campus.chat.cache.UserInfoCache;
import com.ccj.campus.chat.dao.MessageDao;
import com.ccj.campus.chat.dto.ImageMessageDto;
import com.ccj.campus.chat.entity.MessageExtra;
import com.ccj.campus.chat.entity.Messages;
import com.ccj.campus.chat.entity.Users;
import com.ccj.campus.chat.enums.MessageTypeEnum;
import com.ccj.campus.chat.service.MessagesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @Author ccj
 * @Date 2024-06-08 14:17
 * @Description
 */
@Component
@RequiredArgsConstructor
public class ImageMessageHandler extends AbstractMessageHandler<ImageMessageDto> {

    private final MessagesService messagesService;
    private final UserInfoCache userInfoCache;
    private final MessageDao messageDao;

    @Override
    public Object showMsg(Messages msg) {
        return msg.getExtra().getImageMessage();
    }

    @Override
    public Object showReplyMsg(Messages msg) {
        Users user = userInfoCache.get(msg.getFromUid());
        return user.getFullName() + ": [图片]";
    }

    @Override
    public String showContactMsg(Long uid, Messages msg) {
        String prefix = messagesService.getRecallMessageUserName(uid, msg);
        return prefix + "[图片]";
    }

    @Override
    public MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.IMG;
    }

    @Override
    public void saveMessage(Messages message, ImageMessageDto imageMessageDto) {
        MessageExtra extra = Optional.ofNullable(message.getExtra()).orElse(new MessageExtra());
        extra.setImageMessage(imageMessageDto);

        Messages update = new Messages();
        update.setId(message.getId());
        update.setExtra(extra);

        messageDao.updateMessageFieldsById(update.getId(), null, null, null, null, null, update.getExtra());
    }
}
