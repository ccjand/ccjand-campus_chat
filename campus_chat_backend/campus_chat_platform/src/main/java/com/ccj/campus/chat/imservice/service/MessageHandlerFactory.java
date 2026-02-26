package com.ccj.campus.chat.imservice.service;


import com.ccj.campus.chat.strategy.AbstractMessageHandler;
import com.ccj.campus.chat.utils.AssertUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author ccj
 * @Date 2024-05-07 16:49
 * @Description
 */
public class MessageHandlerFactory {

    public static Map<Integer, AbstractMessageHandler<?>> HANDLERS = new HashMap<>();


    public static AbstractMessageHandler<?> getMessageHandlerNonNull(Integer msgType) {
        AbstractMessageHandler<?> handler = HANDLERS.get(msgType);
        AssertUtil.isNotEmpty(handler, "不支持的消息类型 messageType:" + msgType);
        return handler;
    }

    public static void register(Integer typeCode, AbstractMessageHandler handler) {
        HANDLERS.putIfAbsent(typeCode, handler);
    }
}
