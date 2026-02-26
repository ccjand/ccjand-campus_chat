package com.ccj.campus.chat.imservice;

import cn.hutool.core.net.url.UrlBuilder;
import com.ccj.campus.chat.utils.NettyUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * @Author ccj
 * @Date 2024-04-07 15:27
 * @Description 协议升级之前进行相关处理
 */
public class HttpHeadersHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //是http协议请求, 即未升级为websocket协议之前的第一次连接
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;

            UrlBuilder urlBuilder = UrlBuilder.ofHttp(request.uri());
            //获取请求头的 token 值
            Optional<String> tokenOptional = getTokenOptional(urlBuilder, request);

            tokenOptional.ifPresentOrElse(token -> NettyUtils.setAttr(ctx.channel(), NettyUtils.TOKEN, token),
                    ctx::fireChannelInactive);

            /**
             将 xxx?key=value 变成 不带参数的路径
             例如：本来是ws://127.0.0.1:8090/im?token=xxx, 重写为ws://127.0.0.1:8090/im,
             才能被下游的netty 自带 WebSocketServerProtocolHandler("/im")发现,
             被netty的【WebSocketServerProtocolHandshakeHandler的channelRead方法】处理后触发握手完成事件,
             才能被我写的 【NettyWebSocketServerHandler的userEventTriggered方法】捕获事件并处理
             @see com.ccj.chat.moon.websocket.NettyWebSocketServerHandler#userEventTriggered(ChannelHandlerContext, Object)
             @see WebSocketServerProtocolHandshakeHandler#channelRead(ChannelHandlerContext, Object)
             */
            //如果new WebSocketServerProtocolHandler("/im", true)配置了true，这里就不用重写路径了
            String rewritePath = urlBuilder.getPath().toString();
            request.setUri(rewritePath);


            //X-Real-IP:客户端通过nginx转发到服务器的
            String ip = request.headers().get("X-Real-IP");

            if (StringUtils.isBlank(ip)) {
                //客户端是直连服务器的
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                ip = address.getAddress().getHostAddress();
            }

            NettyUtils.setAttr(ctx.channel(), NettyUtils.IP, ip);//保存用户真实ip到channel附件
            //TODO 后期优化使用自定义协议, 手机app、平板、桌面应用都有对应的不同的通信协议
            getTerminalType(urlBuilder, request)
                    .ifPresentOrElse((terminalType) -> NettyUtils.setAttr(ctx.channel(), NettyUtils.TERMINAL_TYPE, Integer.parseInt(terminalType)), ctx::fireChannelInactive);


            //这个处理器只会在协议升级之前执行一次, 后续不会再使用, 直接移除
            ctx.pipeline().remove(this);

            //继续往下游处理器链传递
            if (ctx.channel().isActive()) {
                ctx.fireChannelRead(msg);
            }
        } else if (msg instanceof WebSocketFrame) {
            // 处理websocket后续的消息
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }


    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // Check for closing frame 关闭
        if (frame instanceof CloseWebSocketFrame) {
            ctx.fireChannelInactive();
            return;
        }
        if (frame instanceof PingWebSocketFrame) { // ping/pong作为心跳
            ctx.write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 不处理二进制消息
        if (frame instanceof BinaryWebSocketFrame) {
            ctx.write(frame.retain());
        }
    }

    private static Optional<String> getTerminalType(UrlBuilder urlBuilder, FullHttpRequest request) {
        return Optional.ofNullable(urlBuilder.getQuery())
                .map(t -> {
                    CharSequence terminalType = t.get("terminalType");
                    if (terminalType != null && terminalType.length() > 0) {
                        return terminalType.toString();
                    }

                    return request.headers().get("terminalType");
                });
    }

    private static Optional<String> getTokenOptional(UrlBuilder urlBuilder, FullHttpRequest request) {


        return Optional.ofNullable(urlBuilder.getQuery())
                .map(uri -> {
                    CharSequence token = uri.get("token");
                    if (token != null && token.length() > 0) {
                        return token.toString();
                    }
                    return request.headers().get("token");
                });
    }
}
