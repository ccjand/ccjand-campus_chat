package com.ccj.campus.chat.redissonlock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @Author ccj
 * @Date 2024-04-11 15:56
 * @Description 自定义的分布式锁注解
 */
@Retention(RetentionPolicy.RUNTIME) //运行时生效
@Target({ElementType.METHOD}) //作用于方法上
public @interface RedissonLock {

    /**
     * key 前缀, 默认取方法全限定名，除非我们在不同方法上对同一个资源做分布式锁，就自己指定
     */
    String prefixKey() default "";

    /**
     * key的值获取遵循 spring 的 EL 表达式
     */
    String key() default "";

    /**
     * 获取锁等待时间, 默认不等待, 拿不到就快速失败
     */
    int waitTime() default -1;

    /**
     * 获取锁等待时间单位, 默认毫秒
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
