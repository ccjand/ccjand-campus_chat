package com.ccj.campus.chat.dto;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @Author ccj
 * @Date 2024-05-07 17:21
 * @Description
 */
@Getter
public class SendMessageEvent extends ApplicationEvent {

    private final Integer roomType;
    private final Long messageId;
    private final Long roomMsgSeq;
    private final Long timestampInSeconds;
    private final Boolean rectification;
    private final Integer random;
    private final Long roomId;

    public SendMessageEvent(Object source, Integer roomType, Long messageId, Long roomMsgSeq, Long timestampInSeconds, Boolean rectification, Integer random, Long roomId) {
        super(source);
        this.roomType = roomType;
        this.messageId = messageId;
        this.roomMsgSeq = roomMsgSeq;
        this.timestampInSeconds = timestampInSeconds;
        this.rectification = rectification;
        this.random = random;
        this.roomId = roomId;
    }
}
