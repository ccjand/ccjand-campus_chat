package com.ccj.campus.chat;

import com.sankuai.inf.leaf.plugin.annotation.EnableLeafServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.nio.file.Paths;

/**
 * @Author ccj
 * @Date 2024-04-16 17:34
 * @Description csdn 的教程 https://blog.csdn.net/lzb820/article/details/113875150
 */
@EnableLeafServer
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class LeafApplication {


    public static void main(String[] args) {
        initNacosClientPaths();
        SpringApplication.run(LeafApplication.class, args);
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
