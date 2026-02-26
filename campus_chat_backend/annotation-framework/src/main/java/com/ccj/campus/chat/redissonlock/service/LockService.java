package com.ccj.campus.chat.redissonlock.service;


import lombok.SneakyThrows;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @Author ccj
 * @Date 2024-04-11 15:01
 * @Description
 */
@Service
public class LockService {

    @Autowired
    private RedissonClient redissonClient;

    public <T> T executeWithLockThrows(String key, Integer waitTime, TimeUnit timeUnit, SupplierThrow<T> supplierThrow) throws Throwable {
        RLock lock = redissonClient.getLock(key);
        boolean successful;
        if (waitTime > 0) {
            successful = lock.tryLock(waitTime, timeUnit);
        } else {
            successful = lock.tryLock();
        }

        if (!successful) {
            throw new RuntimeException("请求操作太频繁了,休息下吧~~");
        }

        try {
            return supplierThrow.get();
        } finally {
            lock.unlock();
        }
    }

    @SneakyThrows
    public <T> T executeWithLock(String key, Integer waitTime, TimeUnit timeUnit, Supplier<T> supplier) {
        return executeWithLockThrows(key, waitTime, timeUnit, supplier::get);
    }


    public void executeWithLock(String key, Runnable runnable) {
        executeWithLock(key, -1, null, () -> {
            runnable.run();
            return null;
        });
    }


    @FunctionalInterface
    public interface SupplierThrow<T> {
        T get() throws Throwable;
    }
}
