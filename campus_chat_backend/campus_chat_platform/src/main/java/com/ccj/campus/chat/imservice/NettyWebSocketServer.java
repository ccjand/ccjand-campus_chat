package com.ccj.campus.chat.imservice;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.websocket.server.ServerEndpoint;
import java.net.InetAddress;
import java.util.Properties;

/**
 * @Author ccj
 * @Date 2024-04-03 17:35
 * @Description
 */
@Configuration
@Slf4j
@ServerEndpoint(value = "/im", subprotocols = {"protocol"})
public class NettyWebSocketServer {

    public Integer WEB_SOCKET_PORT = 8090;

    public String WEB_SOCKET_SERVER_NAME = "im-service";

    public static final IMChannelHandler NETTY_WEB_SOCKET_SERVER_HANDLER = new IMChannelHandler();
    // 创建线程池执行器

    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup(NettyRuntime.availableProcessors());

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;


    //    @PostConstruct
    @Async
    public void start() throws InterruptedException {
        run();
    }


    public void run() throws InterruptedException {
        // 服务器启动引导对象
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true) //
                .option(ChannelOption.SO_BACKLOG, 3000)
                .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT) // 调整 netty 缓冲区
                .option(ChannelOption.SO_REUSEADDR, true)//让端口在关闭后立即被重用，避免TIME_WAIT状态导致的端口不可用问题
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new LoggingHandler(LogLevel.INFO)) // 为 bossGroup 添加 日志处理器
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //30秒客户端没有向服务器发送心跳则关闭连接
//                        pipeline.addLast(new IdleStateHandler(30, 0, 180));
                        // 因为使用http协议，所以需要使用http的编码器，解码器
                        pipeline.addLast(new HttpServerCodec());
                        // 以块方式写，添加 chunkedWriter 处理器
                        pipeline.addLast(new ChunkedWriteHandler());
                        //设置2min的超时时间，如果某个通道2min内未发送信号，则抛出异常删除当前通道
//                        pipeline.addLast(new ReadTimeoutHandler(120));


                        pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                        pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                        /**
                         * 说明：
                         *  1. http数据在传输过程中是分段的，HttpObjectAggregator可以把多个段聚合起来；
                         *  2. 这就是为什么当浏览器发送大量数据时，就会发出多次 http请求的原因
                         */
                        pipeline.addLast(new HttpObjectAggregator(5242880));//5 * 1024 * 1024 = 5MB

                        //自定义请求头处理器
                        pipeline.addLast(new HttpHeadersHandler());

                        /**
                         * 说明：
                         *  1. 对于 WebSocket，它的数据是以帧frame 的形式传递的；
                         *  2. 可以看到 WebSocketFrame 下面有6个子类
                         *  3. 浏览器发送请求时： ws://localhost:8090/hello 表示请求的uri
                         *  4. WebSocketServerProtocolHandler 核心功能是把 http协议升级为 ws 协议，保持长连接；
                         *      是通过一个状态码 101 来切换的
                         */
                        //通过 WebSocketServerProtocolHandler将http协议请求升级为websocket协议
                        //pipeline.addLast(new WebSocketServerProtocolHandler("/im", true));//这里如果配置了true，就不用重写路径了
                        pipeline.addLast(new WebSocketServerProtocolHandler("/im"));//此路径下的请求都转成ws协议请求
                        // 自定义handler ，处理业务逻辑
                        pipeline.addLast(NETTY_WEB_SOCKET_SERVER_HANDLER);
                    }
                });


        //注册到nacos
        registerNamingService(WEB_SOCKET_SERVER_NAME, WEB_SOCKET_PORT);
        // 启动服务器，监听端口，阻塞直到启动成功
        // 建立连接，一个bootstrap可以建立多个channel
        serverBootstrap.bind(WEB_SOCKET_PORT)
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.channel().isActive()) {
                            future.channel().close();
                        }
                    }
                })
                .sync();
    }


    /**
     * 将Netty服务注册进Nacos
     */
    private void registerNamingService(String nettyName, Integer nettyPort) {
        try {
            Properties properties = new Properties();
            properties.setProperty(PropertyKeyConst.SERVER_ADDR, nacosDiscoveryProperties.getServerAddr());
            properties.setProperty(PropertyKeyConst.NAMESPACE, nacosDiscoveryProperties.getNamespace());
            NamingService namingService = NamingFactory.createNamingService(properties);
            InetAddress address = InetAddress.getLocalHost();
            namingService.registerInstance(nettyName, nacosDiscoveryProperties.getGroup(), address.getHostAddress(), nettyPort);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void destroy() {
        Future<?> future1 = bossGroup.shutdownGracefully();
        Future<?> future2 = workerGroup.shutdownGracefully();
        future1.syncUninterruptibly();
        future2.syncUninterruptibly();

        log.info("关闭 ws server 成功");
    }


}
