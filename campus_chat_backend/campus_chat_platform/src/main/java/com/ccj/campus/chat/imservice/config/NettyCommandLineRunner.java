package com.ccj.campus.chat.imservice.config;

import com.ccj.campus.chat.imservice.NettyWebSocketServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author ccj
 * @Date 2024-06-18 23:06
 * @Description
 */
@Component
public class NettyCommandLineRunner implements CommandLineRunner {

    @Resource
    private NettyWebSocketServer nettyWebSocketServer;


    @Override
    public void run(String... args) throws Exception {
        //服务端启动的端口不可和Springboot启动类的端口号重复
        nettyWebSocketServer.start();
        //关闭服务器的时候同时关闭Netty服务
        Runtime.getRuntime().addShutdownHook(new Thread(() -> nettyWebSocketServer.destroy()));
    }
}
