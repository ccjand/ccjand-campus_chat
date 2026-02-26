package com.ccj.campus.chat.imservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author ccj
 * @Date 2024-05-03 15:14
 * @Description
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WSChannelExtraDTO {
    private Long uid;
    private Integer terminalType;
}
