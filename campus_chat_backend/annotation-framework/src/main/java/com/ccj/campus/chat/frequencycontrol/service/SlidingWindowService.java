package com.ccj.campus.chat.frequencycontrol.service;

import com.ccj.campus.chat.frequencycontrol.entity.FrequencyStrategy;
import com.ccj.campus.chat.frequencycontrol.entity.dto.SlidingWindowDTO;
import com.ccj.campus.chat.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author ccj
 * @Date 2024-07-24 17:33
 * @Description
 */
@Slf4j
@Component
public class SlidingWindowService extends AbstractFrequencyService<SlidingWindowDTO> {
    @Override
    public String getStrategyName() {
        return FrequencyStrategy.SLIDING_WINDOW;
    }

    @Override
    public boolean reachedThreshold(Map<String, SlidingWindowDTO> ruleMap) {
        // 批量获取redis统计的值
        List<String> frequencyKeys = new ArrayList<>(ruleMap.keySet());
        for (String key : frequencyKeys) {
            SlidingWindowDTO controlDTO = ruleMap.get(key);
            // 获取窗口时间内计数
            Long count = RedisUtils.zCard(key);
            int threshold = controlDTO.getCount();
            if (Objects.nonNull(count) && count >= threshold) {
                //频率超过了
                log.warn("滑动窗口限流  key:{}, count:{}, 阈值:{}", key, count, threshold);
                return true;
            }
        }
        return false;

    }

    @Override
    public void increaseFrequencyCount(Map<String, SlidingWindowDTO> ruleMap) {
        List<String> frequencyKeys = new ArrayList<>(ruleMap.keySet());
        for (String key : frequencyKeys) {
            SlidingWindowDTO controlDTO = ruleMap.get(key);
            // 窗口最小周期转秒
            long period = controlDTO.getUnit().toMillis(controlDTO.getPeriod());
            long current = System.currentTimeMillis();
            // 窗口大小 单位 秒
            long length = period * controlDTO.getWindowSize();
            long start = current - length;
            long expireTime = length + period;
            RedisUtils.zAdd(key, String.valueOf(current), current);
            // 删除周期之前的数据
            RedisUtils.zRemoveRangeByScore(key, 0, start);
            // 过期时间窗口长度+时间间隔
            RedisUtils.expire(key, expireTime, TimeUnit.MILLISECONDS);
        }
    }

}
