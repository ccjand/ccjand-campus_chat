package com.ccj.campus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author ccj
 * @Date 2024-07-18 23:10
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageCheckDto {

    private Long findMessageId;
    private Integer roomType;
}
