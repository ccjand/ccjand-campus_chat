package com.ccj.campus.chat.frequencycontrol.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author ccj
 * @Date 2024-07-24 12:25
 * @Description
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class TokenBucketDTO extends FrequencyRuleDTO {

    private final long capacity; // 令牌桶容量
    private final double refillRate; // 每秒补充的令牌数
    private double tokens; // 当前令牌数量
    private long lastRefillTime; // 上次补充令牌的时间

    private final ReentrantLock lock = new ReentrantLock();

    public TokenBucketDTO(long capacity, double refillRate) {
        if (capacity <= 0 || refillRate <= 0) {
            throw new RuntimeException("capacity and refillRate must be greater than 0");
        }
        this.capacity = capacity;
        this.refillRate = refillRate;

        this.tokens = capacity;
        this.lastRefillTime = System.nanoTime();
    }

    /**
     * 补充令牌
     */
    private void refillTokens() {
        long currentTime = System.nanoTime();
        // 转换为秒 , 距离上一次补充有多久的时间了
        double elapsedTime = (currentTime - lastRefillTime) / 1e9;
        double tokensToAdd = elapsedTime * refillRate; //类似速度*时间=路程, 表示需要补充多少令牌
        log.info("tokensToAdd is {}", tokensToAdd);
        // 令牌总数不能超过令牌桶容量
        tokens = Math.min(capacity, tokens + tokensToAdd);
        log.info("current tokens is {}", tokens);
        lastRefillTime = currentTime;
    }

    /**
     * 尝试获取令牌
     *
     * @param permits 需要获取的令牌数量
     * @return 是否获取成功
     */
    public boolean tryAcquire(int permits) {
        lock.lock();
        try {
            refillTokens();
            return tokens < permits;
        } finally {
            lock.unlock();
        }
    }

    public void deductToken(int permits) {
        lock.lock();
        try {
            if (tokens >= permits) {
                tokens -= permits;
            } else {
                // 可以考虑抛出异常或记录日志，因为试图消费的令牌比实际拥有的多
                throw new RuntimeException("没有足够的令牌可以消费了");
            }
        } finally {
            lock.unlock();
        }
    }

}
