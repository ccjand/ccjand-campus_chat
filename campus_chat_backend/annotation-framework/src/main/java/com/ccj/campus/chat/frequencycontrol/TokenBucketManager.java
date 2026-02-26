package com.ccj.campus.chat.frequencycontrol;


import com.ccj.campus.chat.frequencycontrol.entity.dto.TokenBucketDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author ccj
 * @Date 2024-07-24 16:09
 * @Description
 */
@Component
public class TokenBucketManager {

    private final Map<String, TokenBucketDTO> tokenBucketMap = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();


    public void createTokenBucket(String key, long capacity, double refillRate) {
        lock.lock();
        try {
            if (tokenBucketMap.containsKey(key)) {
                return;
            }

            TokenBucketDTO tokenBucketDTO = new TokenBucketDTO(capacity, refillRate);
            tokenBucketMap.put(key, tokenBucketDTO);
        } finally {
            lock.unlock();
        }
    }

    public void removeTokenBucket(String key) {
        lock.lock();
        try {
            tokenBucketMap.remove(key);
        } finally {
            lock.unlock();
        }
    }


    public boolean tryAcquire(String key, int permits) {

        TokenBucketDTO tokenBucketDTO = tokenBucketMap.get(key);
        if (tokenBucketDTO == null) {
            return false;
        }
        return tokenBucketDTO.tryAcquire(permits);
    }

    public void deductToken(String key, int permits) {
        TokenBucketDTO tokenBucketDTO = tokenBucketMap.get(key);
        if (tokenBucketDTO == null) {
            return;
        }
        tokenBucketDTO.deductToken(permits);
    }

}
