package com.ccj.campus.chat.utils;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * @Author ccj
 * @Date 2024-04-07 16:16
 * @Description
 */
public class NettyUtils {

    public static final AttributeKey<String> TOKEN = AttributeKey.valueOf("token");
    public static final AttributeKey<String> IP = AttributeKey.valueOf("ip");
    public static final AttributeKey<Integer> TERMINAL_TYPE = AttributeKey.valueOf("terminal_type");
    public static final AttributeKey<Long> UID = AttributeKey.valueOf("uid");

    public static <T> T getAttr(Channel channel, AttributeKey<T> attrKey) {
        Attribute<T> attribute = channel.attr(attrKey);
        return attribute.get();
    }


    public static <T> void setAttr(Channel channel, AttributeKey<T> attrKey, T data) {
        Attribute<T> attribute = channel.attr(attrKey);
        attribute.set(data);
    }
}
