package com.ccj.campus.chat.frequencycontrol.entity.dto;

import lombok.*;

/**
 * @Author ccj
 * @Date 2024-07-24 12:21
 * @Description
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FixedWindowDto extends FrequencyRuleDTO {

    /**
     * 持续时间
     */
    private int time;
}
