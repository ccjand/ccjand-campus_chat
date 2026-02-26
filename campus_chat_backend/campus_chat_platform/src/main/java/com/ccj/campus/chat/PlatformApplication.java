package com.ccj.campus.chat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.nio.file.Paths;

/**
 * @Author ccj
 * @Date 2026-01-06 15:03
 * @Description
 */
@SpringBootApplication
@MapperScan(basePackages = "com.ccj.campus.chat.mapper")
@ConfigurationPropertiesScan(basePackages = "com.ccj.campus.chat.config")//配置类扫描
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableDiscoveryClient
public class PlatformApplication {
    public static void main(String[] args) {
        initNacosClientPaths();
        SpringApplication.run(PlatformApplication.class, args);
    }

    private static void initNacosClientPaths() {
        String appTmpDir = Paths.get(System.getProperty("java.io.tmpdir"), "campus-chat").toString();
        String baseDir = Paths.get(appTmpDir, "nacos").toString();
        setPropertyIfAbsent("JM.LOG.PATH", appTmpDir);
        setPropertyIfAbsent("JM.SNAPSHOT.PATH", appTmpDir);
        setPropertyIfAbsent("com.alibaba.nacos.naming.cache.dir", baseDir);
        setPropertyIfAbsent("com.alibaba.nacos.client.naming.cache.dir", baseDir);
        setPropertyIfAbsent("com.alibaba.nacos.client.config.impl.cache.dir", Paths.get(baseDir, "config").toString());
    }

    private static void setPropertyIfAbsent(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }
}
