package com.ccj.campus.chat.frequencycontrol.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author ccj
 * @Date 2024-07-24 12:23
 * @Description
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SlidingWindowDTO extends FrequencyRuleDTO {

    /**
     * 窗口大小, 默认10s
     */
    private int windowSize;

    /**
     * 窗口最小周期 1s (窗口大小是 10s， 1s一个小格子，-共10个格子)
     */
    private int period;
}
