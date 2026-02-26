package com.ccj.campus.chat.imservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-05-13 16:06
 * @Description
 */
@AllArgsConstructor
@Getter
public enum WSPushTypeEnum {

    /**
     * 个人
     */
    PERSONAL(1),


    /**
     * 全员
     */
    ALL(2);

    private final Integer type;

    public static final Map<Integer, WSPushTypeEnum> cache;

    static {
        cache = Arrays.stream(values()).collect(Collectors.toMap(WSPushTypeEnum::getType, v -> v));

    }

    public static WSPushTypeEnum of(Integer type) {
        return cache.get(type);
    }
}
