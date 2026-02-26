package com.ccj.campus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Author ccj
 * @Date 2024-05-09 21:34
 * @Description 撤回消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecallMessageDto implements Serializable {

    private static final long serialVersionUID = 1L;


    private Long recallMessageId;

    /**
     * 撤回时间
     */
    private LocalDateTime recallTime;
}
