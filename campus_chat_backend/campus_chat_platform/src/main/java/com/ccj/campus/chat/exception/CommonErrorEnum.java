package com.ccj.campus.chat.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author ccj
 * @Date 2024-04-10 15:45
 * @Description
 */
@AllArgsConstructor
@Getter
public enum CommonErrorEnum implements ErrorEnum {
    BUSINESS_ERROR(0, "{0}"),
    SYSTEM_ERROR(-1, "系统开小差了,请稍候重试哦~~"),
    INVALID_PARAM(-2, "参数错误了呢,再检查下吧~~"),
    LOCK_LIMIT(-3, "请求操作太频繁了,休息下吧~~"),
    INVALID_OPERATION(-4, "非法操作"),
    NOT_FRIEND(-5, "你们还不是好友"),
    ROOM_NOT_EXIST(-6, "房间不存在"),
    ROOM_FRIEND_NOT_EXIST(-7, "好友房间不存在"),
    ROOM_GROUP_NOT_EXIST(-8, "群聊房间不存在"),
    ;

    private final Integer code;
    private final String desc;

    @Override
    public Integer getErrorCode() {
        return code;
    }

    @Override
    public String getErrorMsg() {
        return desc;
    }
}
