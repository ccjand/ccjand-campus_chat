package com.ccj.campus.chat.dto;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @Author ccj
 * @Date 2024-05-16 15:37
 * @Description
 */
@Getter
public class RecallMessageEvent extends ApplicationEvent {

    private final RecallChatMessageDto recallMessage;

    public RecallMessageEvent(Object source, RecallChatMessageDto recallMessage) {
        super(source);
        this.recallMessage = recallMessage;
    }
}
