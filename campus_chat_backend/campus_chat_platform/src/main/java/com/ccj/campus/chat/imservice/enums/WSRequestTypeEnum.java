package com.ccj.campus.chat.imservice.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-05-04 12:31
 * @Description websocket请求的类别
 */
@AllArgsConstructor
@Getter
public enum WSRequestTypeEnum {
    LOGIN(1),//请求登录二维码（PC端）
    AUTHORIZE(2),//登录认证（PC端）
    HEARTBEAT(3),//心跳包（所有端）
    PULL_INTERVAL_MESSAGE_REQ(4), // 拉取增量消息（适用于在聊天界面拉取, 并非客户端轮训）
    PULL_REMAIN_MESSAGE_REQ(5), //请求拉取剩下的消息（客户端轮训拉取）
    OFFLINE_MESSAGE(6),//离线消息推送
    SEND_MESSAGE(7),//发送聊天消息（所有端）


    //=======================（私聊）（一对一）语音通话（所有端）==============================
    PRIVATE_VOICE_CALL_REQUEST(14),//请求
    RESPONSE_PRIVATE_VOICE_CALL_NOTIFY(15), //回应通知 【这样才能知道是接听了还是拒绝了】
    PRIVATE_VOICE_CALL_RESPONSE(16),//响应


    //========================（群聊）（多对多）语音通话（所有端）===========================
    GROUP_VOICE_CALL_REQUEST(17),//请求
    RESPONSE_GROUP_VOICE_CALL_NOTIFY(18),//通知
    GROUP_VOICE_CALL_RESPONSE(19),//响应


    //========================（私聊）（一对一）视频电话（所有端）===========================
    PRIVATE_VIDEO_CALL_REQUEST(20),//请求
    RESPONSE_PRIVATE_VIDEO_CALL_NOTIFY(21),//通知
    PRIVATE_VIDEO_CALL_RESPONSE(22),//响应


    //========================（群聊）（多对多）视频电话（所有端）===========================
    GROUP_VIDEO_CALL_REQUEST(23),//请求
    RESPONSE_GROUP_VIDEO_CALL_NOTIFY(24),//通知
    GROUP_VIDEO_CALL_RESPONSE(25),//响应

    //========================结束音视频通话（前提是已经接听了）===========================
    FINISH_CALL(26),


    ;

    private final Integer type;

    final static Map<Integer, WSRequestTypeEnum> cache;

    static {
        cache = Arrays.stream(WSRequestTypeEnum.values()).collect(Collectors.toMap(WSRequestTypeEnum::getType, Function.identity()));
    }


    public static WSRequestTypeEnum of(Integer type) {
        return cache.get(type);
    }

}
