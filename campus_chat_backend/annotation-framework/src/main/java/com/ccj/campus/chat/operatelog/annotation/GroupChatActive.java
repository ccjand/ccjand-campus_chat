package com.ccj.campus.chat.operatelog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author ccj
 * @Date 2024-07-05 16:42
 * @Description 采集用户行为
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GroupChatActive {
    String roomId() default "";
}
