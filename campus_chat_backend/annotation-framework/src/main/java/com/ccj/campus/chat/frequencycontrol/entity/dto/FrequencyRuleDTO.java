package com.ccj.campus.chat.frequencycontrol.entity.dto;

import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * @Author ccj
 * @Date 2024-07-24 12:08
 * @Description
 */
@Data
public class FrequencyRuleDTO {

    /**
     * @see com.ccj.campus.chat.frequencycontrol.annotation.FrequencyRule.LimitTarget
     * 根据target的类型, 用于构建不同的redis的key
     */
    private String key;

    /**
     * 频控的时间单位
     */
    private TimeUnit unit;

    /**
     * 单位时间内允许的访问次数
     */
    private int count;
}
