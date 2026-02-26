package com.ccj.campus.chat.frequencycontrol;


import com.ccj.campus.chat.frequencycontrol.entity.dto.FrequencyRuleDTO;
import com.ccj.campus.chat.frequencycontrol.service.AbstractFrequencyService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author ccj
 * @Date 2024-07-24 14:41
 * @Description 限流策略工厂
 */
public class FrequencyStrategyFactory {

    /**
     * 构造器私有, 防止创建工厂
     */
    private FrequencyStrategyFactory() {

    }

    public static final Map<String, AbstractFrequencyService<?>> frequencyStrategyMap = new ConcurrentHashMap<>(5);


    public static void register(String strategyName, AbstractFrequencyService<?> frequencyService) {
        frequencyStrategyMap.put(strategyName, frequencyService);
    }

    public static <K extends FrequencyRuleDTO> AbstractFrequencyService<K> getFrequencyService(String strategyName) {
        return (AbstractFrequencyService<K>) frequencyStrategyMap.get(strategyName);
    }


}
