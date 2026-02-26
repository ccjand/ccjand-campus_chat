package com.ccj.campus.chat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: 消息类型
 */
@AllArgsConstructor
@Getter
public enum MessageTypeEnum {
    TEXT(1, "文本消息"),
    RECALL(2, "撤回消息"),
    IMG(3, "图片"),
    FILE(4, "文件"),
    VIDEO(6, "本地视频"),
    SYSTEM(8, "系统消息"),

    ;

    private final Integer type;
    private final String desc;

    private static final Map<Integer, MessageTypeEnum> cache;

    static {
        cache = Arrays.stream(MessageTypeEnum.values()).collect(Collectors.toMap(MessageTypeEnum::getType, Function.identity()));
    }

    public static MessageTypeEnum of(Integer type) {
        return cache.get(type);
    }

    public static boolean isSystemMessage(Integer type) {
        return SYSTEM.getType().equals(type);
    }
}
