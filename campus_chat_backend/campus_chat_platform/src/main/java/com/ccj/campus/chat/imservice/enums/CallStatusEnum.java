package com.ccj.campus.chat.imservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-05-23 15:53
 * @Description
 */
@Getter
@AllArgsConstructor
public enum CallStatusEnum {

    accept(1),//接受
    refuse(2),//拒绝
    pending(3),//等待处理
    cancel(4),//取消通话
    finish(5);//结束电话

    private final Integer type;

    private static final Map<Integer, CallStatusEnum> cache;

    static {
        cache = Arrays.stream(CallStatusEnum.values()).collect(Collectors.toMap(CallStatusEnum::getType, v -> v));
    }

    public static CallStatusEnum of(Integer type) {
        return cache.get(type);
    }
}
