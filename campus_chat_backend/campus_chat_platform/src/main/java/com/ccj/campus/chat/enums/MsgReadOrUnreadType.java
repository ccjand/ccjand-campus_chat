package com.ccj.campus.chat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author ccj
 * @Date 2024-06-30 14:33
 * @Description
 */
@Getter
@AllArgsConstructor
public enum MsgReadOrUnreadType {

    read(1),
    unread(2);

    private final Integer type;

    public static boolean isRead(Integer type) {
        return read.getType().equals(type);
    }

    public static boolean isUnread(Integer type) {
        return unread.getType().equals(type);
    }
}
