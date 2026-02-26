package com.ccj.campus.chat.imservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author ccj
 * @Date 2024-04-27 23:12
 * @Description
 */
@Getter
@AllArgsConstructor
public enum UserActiveStatusEnum {

    ONLINE(1, "在线"),
    OFFLINE(2, "离线");

    private final Integer code;
    private final String desc;

    public static UserActiveStatusEnum of(Integer status) {
        for (UserActiveStatusEnum value : UserActiveStatusEnum.values()) {
            if (value.getCode().equals(status)) {
                return value;
            }
        }

        throw new RuntimeException("不支持的状态");
    }

    public static boolean isOnline(UserActiveStatusEnum activeStatusEnum) {
        return ONLINE.equals(activeStatusEnum);
    }

    public static boolean isOffline(UserActiveStatusEnum activeStatusEnum) {
        return OFFLINE.equals(activeStatusEnum);
    }
}
