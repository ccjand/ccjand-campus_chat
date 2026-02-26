package com.ccj.campus.chat.frequencycontrol.annotation;


import com.ccj.campus.chat.frequencycontrol.entity.FrequencyStrategy;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author ccj
 * @Date 2024-07-24 11:21
 * @Description 限流规则
 */
@Repeatable(FrequencyControl.class) //可以重复
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FrequencyRule {

    /**
     * 限流前缀, 默认取方法全限定名，除非我们在不同方法上对同一个资源做频控，就自己指定
     */
    String prefix() default "";

    /**
     * 限流策略, 默认是单位时间内的次数限流
     */
    String strategy() default FrequencyStrategy.TOTAL_COUNT_IN_FIXED_TIME;


    /**
     * 窗口大小, 默认是5个周期
     */
    int windowSize() default 5;

    /**
     * 窗口每个周期大小, 默认是1s
     */
    int period() default 1;

    /**
     * 限流目标, 默认是el表达式
     */
    LimitTarget target() default LimitTarget.EL;

    /**
     * el表达式的值, 默认是空, 使用这个必须指定target是EL
     */
    String elValue() default "";

    /**
     * 频控时间范围, 默认是10s
     */
    int time() default 10;

    /**
     * 频控时间单位, 默认是秒
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 单位时间内的最大请求次数, 默认是10次
     */
    int count() default 1;


    /**
     * 令牌痛, 默认是10
     */
    long capacity() default 10;

    /**
     * 令牌获取速度, 默认是0.5
     */
    double refillRate() default 0.5;

    /**
     * 限流目标
     */
    enum LimitTarget {
        UID, //用户uid
        IP, //用户ip
        EL //spring的el表达式
    }
}

