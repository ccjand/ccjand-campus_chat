package com.ccj.campus.chat.thread;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author ccj
 * @Date 2024-04-07 10:54
 * @Description
 */
@Slf4j
public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Exception: {}", e.getMessage());
    }
}
