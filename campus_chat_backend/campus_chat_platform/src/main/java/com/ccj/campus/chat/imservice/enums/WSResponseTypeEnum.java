package com.ccj.campus.chat.imservice.enums;

import com.ccj.campus.chat.imservice.domain.vo.resp.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-05-03 17:44
 * @Description
 */
@Getter
@AllArgsConstructor
public enum WSResponseTypeEnum {

    ERROR(-1, "异常", null),
    LOGIN_URL(1, "登录二维码返回", WSLoginUrl.class),
    LOGIN_SCAN_SUCCESS(2, "用户扫描成功等待授权", null),
    LOGIN_SUCCESS(3, "用户登录成功返回用户信息", WSLoginSuccess.class),
    PUSH_MODE_MESSAGE(4, "【推模式】新消息", WSMessage.class),
    ONLINE_OFFLINE_NOTIFY(5, "上下线通知", WSOnlineOfflineNotify.class),
    INVALIDATE_TOKEN(6, "使前端的token失效，意味着前端需要重新登录", null),
    BLACK(7, "拉黑用户", WSBlack.class),
    RECALL(9, "消息撤回", WSMsgRecall.class),
    APPLY(10, "好友申请", WSFriendApplication.class),
    MEMBER_CHANGE(11, "成员变动", WSMemberChange.class),
    UNAUTHORIZED(14, "未授权", null),
    HEARTBEAT(15, "心跳包", null),
    PULL_MODE_MESSAGE(16, "【拉模式】【轮训】新消息", WSMessage.class),
    PULL_MODE_INTERVAL_MESSAGE(17, "【拉模式】【非轮训】【房间内增量】新消息", WSMessage.class),
    OFFLINE_MESSAGE(18, "离线消息", WSMessage.class),
    ;

    private final Integer type;
    private final String desc;
    private final Class dataClass;

    private static final Map<Integer, WSResponseTypeEnum> cache;

    static {
        cache = Arrays.stream(WSResponseTypeEnum.values()).collect(Collectors.toMap(WSResponseTypeEnum::getType, Function.identity()));
    }

    public static WSResponseTypeEnum of(Integer type) {
        return cache.get(type);
    }
}
