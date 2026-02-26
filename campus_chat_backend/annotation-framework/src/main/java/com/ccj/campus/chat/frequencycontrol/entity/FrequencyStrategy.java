package com.ccj.campus.chat.frequencycontrol.entity;

import lombok.Getter;

/**
 * @Author ccj
 * @Date 2024-07-24 11:19
 * @Description
 */
@Getter
public class FrequencyStrategy {

    /**
     * 限制规定时间内的次数
     */
    public static final String TOTAL_COUNT_IN_FIXED_TIME = "total_count_in_fixed_time";

    /**
     * 令牌桶
     */
    public static final String TOKEN_BUCKET = "token_bucket";

    /**
     * 滑动窗口
     */
    public static final String SLIDING_WINDOW = "sliding_window";
}
