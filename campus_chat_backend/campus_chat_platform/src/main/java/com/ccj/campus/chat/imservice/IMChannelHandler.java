package com.ccj.campus.chat.imservice;

import cn.hutool.extra.spring.SpringUtil;
import com.ccj.campus.chat.cache.GroupMemberCache;
import com.ccj.campus.chat.cache.RoomCache;
import com.ccj.campus.chat.cache.RoomFriendCache;
import com.ccj.campus.chat.entity.Room;
import com.ccj.campus.chat.entity.RoomFriend;
import com.ccj.campus.chat.enums.RoomTypeEnum;
import com.ccj.campus.chat.exception.BusinessException;
import com.ccj.campus.chat.imservice.domain.vo.req.ChatMessageReq;
import com.ccj.campus.chat.imservice.domain.vo.req.PullIntervalMessageReq;
import com.ccj.campus.chat.imservice.domain.vo.req.PullMessageListReq;
import com.ccj.campus.chat.imservice.domain.vo.resp.ChatMessageResp;
import com.ccj.campus.chat.imservice.domain.vo.resp.WSBaseResponse;
import com.ccj.campus.chat.imservice.enums.WSRequestTypeEnum;
import com.ccj.campus.chat.imservice.enums.WSResponseTypeEnum;
import com.ccj.campus.chat.imservice.service.WebSocketService;
import com.ccj.campus.chat.imservice.service.adapter.WebSocketAdapter;
import com.ccj.campus.chat.service.ChatService;
import com.ccj.campus.chat.util.JsonUtils;
import com.ccj.campus.chat.utils.AssertUtil;
import com.ccj.campus.chat.utils.NettyUtils;
import com.fasterxml.jackson.databind.JsonNode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author ccj
 * @Date 2024-05-03 14:26
 * @Description
 */
@Slf4j
@Sharable
public class IMChannelHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private WebSocketService webSocketService;
    private ChatService chatService;
    private RoomCache roomCache;
    private RoomFriendCache roomFriendCache;
    private GroupMemberCache groupMemberCache;


    /**
     * 用户下线
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        userOffline(ctx.channel());
    }


    /**
     * 读取管道数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
//        WSBaseRequest wsBaseRequest = JsonUtils.toObj(msg.text(), WSBaseRequest.class);
        JsonNode jsonNode = JsonUtils.toJsonNode(msg.text());
        JsonNode typeNode = jsonNode.get("type");
        if (typeNode == null) {
            return;
        }
        int type = typeNode.asInt();
        WSRequestTypeEnum requestType = WSRequestTypeEnum.of(type);
        if (requestType == null) {
            log.info("未知的websocket请求类型：{}", type);
            return;
        }
        switch (requestType) {
            case LOGIN -> {
                break;
            }

            case AUTHORIZE -> {
                break;
            }

            case HEARTBEAT -> {
                //回应客户端
                System.out.println("收到心跳包");
                WSBaseResponse<String> heartbeatResp = new WSBaseResponse<>(WSResponseTypeEnum.HEARTBEAT);
                webSocketService.sendMsg(ctx.channel(), heartbeatResp);
            }

            case PULL_INTERVAL_MESSAGE_REQ -> {
                log.debug("收到聊天窗口内的拉取消息请求");
                PullIntervalMessageReq pullMessageListReq =
                        JsonUtils.nodeToValue(jsonNode.get("data"), PullIntervalMessageReq.class);
                webSocketService.requestPullIntervalMessages(ctx.channel(), pullMessageListReq);
            }

            case PULL_REMAIN_MESSAGE_REQ -> {
                log.debug("客户端的群聊轮训拉取");
                PullMessageListReq req = JsonUtils.nodeToValue(jsonNode.get("data"), PullMessageListReq.class);
                webSocketService.pullRemainMessage(ctx.channel(), req);
            }

            case OFFLINE_MESSAGE -> {
                log.debug("客户端的离线消息");
                PullMessageListReq req = JsonUtils.nodeToValue(jsonNode.get("data"), PullMessageListReq.class);
                webSocketService.pushOfflineMessage(ctx.channel(), req);
            }
            case SEND_MESSAGE -> {
                Long uid = NettyUtils.getAttr(ctx.channel(), NettyUtils.UID);
                if (uid == null) {
                    webSocketService.sendMsg(ctx.channel(), WebSocketAdapter.buildAuthErrorResp("未授权"));
                    return;
                }

                try {
                    ChatMessageReq req = JsonUtils.nodeToValue(jsonNode.get("data"), ChatMessageReq.class);
                    AssertUtil.allCheckValidateThrow(req);

                    Long messageId = chatService.sendMessage(uid, req, true);
                    ChatMessageResp resp = chatService.getMessageResp(messageId, uid);

                    WSBaseResponse<ChatMessageResp> wsResp = WebSocketAdapter.buildChatMessageResp(resp);
                    resolveRoomMemberUids(req.getRoomId()).forEach(targetUid -> webSocketService.sendToOnlineUser(targetUid, wsResp));
                } catch (BusinessException e) {
                    webSocketService.sendMsg(ctx.channel(), new WSBaseResponse<>(WSResponseTypeEnum.ERROR, e.getMessage()));
                } catch (Exception e) {
                    log.error("websocket发送消息异常", e);
                    webSocketService.sendMsg(ctx.channel(), new WSBaseResponse<>(WSResponseTypeEnum.ERROR, "系统异常"));
                }
            }
            default -> {
                log.info("未知的websocket请求类型：{}", type);
            }
        }
    }

    /**
     * 用户上线
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        webSocketService = SpringUtil.getBean(WebSocketService.class);
        chatService = SpringUtil.getBean(ChatService.class);
        roomCache = SpringUtil.getBean(RoomCache.class);
        roomFriendCache = SpringUtil.getBean(RoomFriendCache.class);
        groupMemberCache = SpringUtil.getBean(GroupMemberCache.class);
        webSocketService.connect(ctx.channel());
    }

    /**
     * 用户下线
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Long uid = NettyUtils.getAttr(ctx.channel(), NettyUtils.UID);
        log.info("用户下线 uid：{}", uid);
        userOffline(ctx.channel());
    }


    /**
     * 捕获WebSocket事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            //从管道获取token
            String token = NettyUtils.getAttr(ctx.channel(), NettyUtils.TOKEN);
            if (StringUtils.isNotBlank(token)) {
                log.info("用户{} 信息认证成功", token);
                webSocketService.authorize(ctx.channel(), token);
            }
        } else if (evt instanceof IdleStateEvent ise) {
            //用户空闲
            if (ise.state() == IdleState.READER_IDLE) {
                System.out.println("客户端超30s没心跳, 触发读空闲事件");
                //强制用户下线, 避免占用连接
                userOffline(ctx.channel());
            }
        }
    }

    private void userOffline(Channel channel) {
        webSocketService.remove(channel);
        //推送离线消息, 客户端收到后, 再次重新连接
        //webSocketService.sendOfflineMessage(channel);
        //关闭通道
        channel.close();
    }

    private java.util.Set<Long> resolveRoomMemberUids(Long roomId) {
        java.util.Set<Long> uidSet = new java.util.HashSet<>();
        Room room = roomCache.get(roomId);
        if (room == null) {
            return uidSet;
        }

        if (RoomTypeEnum.isGroupRoom(room.getType())) {
            uidSet.addAll(groupMemberCache.getGroupMember(roomId));
            return uidSet;
        }

        RoomFriend roomFriend = roomFriendCache.get(roomId);
        if (roomFriend == null) {
            return uidSet;
        }

        if (roomFriend.getUid1() != null) uidSet.add(roomFriend.getUid1());
        if (roomFriend.getUid2() != null) uidSet.add(roomFriend.getUid2());
        return uidSet;
    }


}
