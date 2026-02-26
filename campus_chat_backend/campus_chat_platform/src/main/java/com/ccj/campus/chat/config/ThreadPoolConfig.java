package com.ccj.campus.chat.config;

import com.ccj.campus.chat.secureinvoke.annotation.SecureInvokeConfigurer;
import com.ccj.campus.chat.thread.MyThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author ccj
 * @Date 2024-04-07 10:14
 * @Description
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig implements AsyncConfigurer, SecureInvokeConfigurer {

    /**
     * 项目共用线程池
     */
    public static final String CAMPUS_CHAT_EXECUTOR = "CAMPUS_CHAT_EXECUTOR";

    /**
     * websocket通信线程池
     */
    public static final String WS_EXECUTOR = "websocketExecutor";


    /**
     * 安全重试的线程池
     */
    public static final String RETRY_EXECUTOR = "retryExecutor";


    /**
     * 重新构建缓存的线程池
     */
    public static final String REBUILD_CACHE_EXECUTOR = "rebuildExecutor";


    @Override
    public Executor getAsyncExecutor() {
        return campusChatThreadPool();
    }


    @Override
    public Executor getSecureInvokeExecutor() {
        return retryExecutor();
    }

    @Bean(CAMPUS_CHAT_EXECUTOR)
    @Primary
    public ThreadPoolTaskExecutor campusChatThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        //线程名前缀
        executor.setThreadNamePrefix("chat-executor-");
        //如果线程池满了， 任务哪个线程提交过来的谁执行【认为任务很重要，不可丢弃】
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //优雅停机，关闭容器会等待线程池的任务全部执行完再关闭线程池，此时不会再接受新任务
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //统一管理线程任务的异常日志打印
        executor.setThreadFactory(new MyThreadFactory(executor));
        executor.initialize();
        return executor;
    }


    @Bean(WS_EXECUTOR)
    public ThreadPoolTaskExecutor websocketExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(16);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(1000);//支持同时推送1000人
        executor.setThreadNamePrefix("websocket-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());//满了直接丢弃，默认为不重要消息推送
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadFactory(new MyThreadFactory(executor));
        executor.initialize();
        return executor;
    }


    @Bean(RETRY_EXECUTOR)
    public ThreadPoolTaskExecutor retryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        //线程名前缀
        executor.setThreadNamePrefix("retry-executor-");
        //如果线程池满了， 任务哪个线程提交过来的谁执行【认为任务很重要，不可丢弃】
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //优雅停机，关闭容器会等待线程池的任务全部执行完再关闭线程池，此时不会再接受新任务
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //统一管理线程任务的异常日志打印
        executor.setThreadFactory(new MyThreadFactory(executor));
        executor.initialize();
        return executor;
    }


    @Bean(REBUILD_CACHE_EXECUTOR)
    public ThreadPoolTaskExecutor rebuildExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        //线程名前缀
        executor.setThreadNamePrefix("rebuild-cache-executor-");
        //如果线程池满了， 任务哪个线程提交过来的谁执行【认为任务很重要，不可丢弃】
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //优雅停机，关闭容器会等待线程池的任务全部执行完再关闭线程池，此时不会再接受新任务
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //统一管理线程任务的异常日志打印
        executor.setThreadFactory(new MyThreadFactory(executor));
        executor.initialize();
        return executor;
    }


}