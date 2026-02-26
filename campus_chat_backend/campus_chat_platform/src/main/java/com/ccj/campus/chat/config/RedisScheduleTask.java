package com.ccj.campus.chat.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

/**
 * Redis长时间不连接会断开， 这里弄一个定时任务，定时发送心跳
 */
@Configuration
@EnableScheduling
public class RedisScheduleTask {

    public static final Log log = LogFactory.getLog(RedisScheduleTask.class);

    @Resource
    private StringRedisTemplate dupShowMasterRedisTemplate;

    // 1 minutes
    @Scheduled(fixedRate = 60000)
    private void configureTasks() {
        log.debug("ping redis");
        dupShowMasterRedisTemplate.execute(RedisConnectionCommands::ping);
    }

}

