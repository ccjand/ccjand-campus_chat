package com.ccj.campus.chat.thread;

import lombok.AllArgsConstructor;

import java.util.concurrent.ThreadFactory;

/**
 * @Author ccj
 * @Date 2024-04-07 11:03
 * @Description 使用装饰器模式，在不改变ThreadFactory原有的newThread的情况下，添加上自己的(未捕获的)异常处理
 */
@AllArgsConstructor
public class MyThreadFactory implements ThreadFactory {

    private ThreadFactory original;

    private static final MyUncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER = new MyUncaughtExceptionHandler();

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = original.newThread(r);
        thread.setUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER);
        return thread;
    }
}
