package com.ccj.campus.chat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @Author ccj
 * @Date 2024-05-07 00:22
 * @Description
 */
@Getter
@AllArgsConstructor
public enum RoomTypeEnum {

    GROUP_ROOM(1),//群聊
    SINGLE_ROOM(2);//单聊

    private final Integer type;

    public static boolean isGroupRoom(Integer roomType) {
        return GROUP_ROOM.type.equals(roomType);
    }

    public static boolean isSingleRoom(Integer roomType) {
        return SINGLE_ROOM.type.equals(roomType);
    }

}
