package com.ccj.campus.chat.frequencycontrol.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author ccj
 * @Date 2024-05-04 22:14
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestHolderInfo {
    private Long uid;
    private Integer terminalType;
}
