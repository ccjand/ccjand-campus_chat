package com.ccj.campus.chat.frequencycontrol.service;

import com.ccj.campus.chat.frequencycontrol.entity.FrequencyStrategy;
import com.ccj.campus.chat.frequencycontrol.entity.dto.FixedWindowDto;
import com.ccj.campus.chat.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-07-24 17:19
 * @Description
 */
@Slf4j
@Component
public class TotalCountWithInFixedTimeService extends AbstractFrequencyService<FixedWindowDto> {
    @Override
    public String getStrategyName() {
        return FrequencyStrategy.TOTAL_COUNT_IN_FIXED_TIME;
    }

    @Override
    public boolean reachedThreshold(Map<String, FixedWindowDto> ruleMap) {
        List<String> keys = new ArrayList<>(ruleMap.keySet());
        List<Integer> counts = RedisUtils.mget(keys, Integer.class);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Integer count = counts.get(i);
            int limit = ruleMap.get(key).getCount();
            if (count != null && count >= limit) {
                log.warn("访问限流: key:{}, count:{}, 阈值:{}", key, count, limit);
                return true;
            }
        }
        return false;
    }

    @Override
    public void increaseFrequencyCount(Map<String, FixedWindowDto> ruleMap) {
        ruleMap.forEach((k, v) -> RedisUtils.inc(v.getKey(), v.getTime(), v.getUnit()));
    }
}
