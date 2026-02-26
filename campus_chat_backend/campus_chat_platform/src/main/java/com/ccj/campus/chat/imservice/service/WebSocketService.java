package com.ccj.campus.chat.imservice.service;

import com.ccj.campus.chat.imservice.domain.vo.req.PullIntervalMessageReq;
import com.ccj.campus.chat.imservice.domain.vo.req.PullMessageListReq;
import com.ccj.campus.chat.imservice.domain.vo.resp.WSBaseResponse;
import io.netty.channel.Channel;

/**
 * @Author ccj
 * @Date 2024-05-03 15:11
 * @Description
 */
public interface WebSocketService {

    /**
     * 用户连接
     */
    void connect(Channel channel);


    /**
     * 断开用户连接
     */
    void remove(Channel channel);


    /**
     * 用户授权【用户已经登录成功，只需要携带 token 即可完成授权继续操作】
     *
     * @param channel 客户端通信管道
     * @param token   用户携带的 token
     */
    void authorize(Channel channel, String token);


    /**
     * 发送消息给用户
     *
     * @param uid       用户uid
     * @param wsBaseMsg 消息体
     */
    void sendToOnlineUser(Long uid, WSBaseResponse<?> wsBaseMsg);


    void sendToOnlineUser(Long uid, WSBaseResponse<?> wsBaseMsg, Long skipUid);


    /**
     * 发送消息给客户端
     */
    void sendMsg(Channel channel, WSBaseResponse<?> resp);


    /**
     * 发送离线消息
     */
    void sendOfflineMessage(Channel channel);

    void requestPullIntervalMessages(Channel channel, PullIntervalMessageReq req);

    boolean hasChannel(Long uid);

    void pullRemainMessage(Channel channel, PullMessageListReq req);

    void pushOfflineMessage(Channel channel, PullMessageListReq req);
}
