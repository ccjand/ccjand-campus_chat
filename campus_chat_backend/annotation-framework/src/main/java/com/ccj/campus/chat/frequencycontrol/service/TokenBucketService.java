package com.ccj.campus.chat.frequencycontrol.service;


import com.ccj.campus.chat.frequencycontrol.TokenBucketManager;
import com.ccj.campus.chat.frequencycontrol.entity.FrequencyStrategy;
import com.ccj.campus.chat.frequencycontrol.entity.dto.TokenBucketDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author ccj
 * @Date 2024-07-24 21:07
 * @Description
 */
@Slf4j
@Component
public class TokenBucketService extends AbstractFrequencyService<TokenBucketDTO> {

    @Autowired
    private TokenBucketManager tokenBucketManager;


    @Override
    public String getStrategyName() {
        return FrequencyStrategy.TOKEN_BUCKET;
    }

    @Override
    public boolean reachedThreshold(Map<String, TokenBucketDTO> ruleMap) {
        // 批量获取redis统计的值
        List<String> frequencyKeys = new ArrayList<>(ruleMap.keySet());
        for (String key : frequencyKeys) {
            // 获取 1 个令牌
            return tokenBucketManager.tryAcquire(key, 1);
        }
        return false;
    }

    @Override
    public void increaseFrequencyCount(Map<String, TokenBucketDTO> ruleMap) {
        List<String> frequencyKeys = new ArrayList<>(ruleMap.keySet());
        for (String key : frequencyKeys) {
            TokenBucketDTO tokenBucketDTO = ruleMap.get(key);
            tokenBucketManager.createTokenBucket(key, tokenBucketDTO.getCapacity(), tokenBucketDTO.getRefillRate());
            // 扣减 1 个令牌
            tokenBucketManager.deductToken(key, 1);
        }
    }
}
