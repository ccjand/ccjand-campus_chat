package com.ccj.campus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author ccj
 * @Date 2024-05-16 15:45
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecallChatMessageDto implements Serializable {

    /**
     * 谁撤回的
     */
    private Long fromUid;

    /**
     * 房间id
     */
    private Long roomId;

    /**
     * 撤回的消息id
     */
    private Long recallMessageId;
}
