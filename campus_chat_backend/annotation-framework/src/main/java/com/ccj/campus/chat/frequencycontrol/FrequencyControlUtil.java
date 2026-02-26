package com.ccj.campus.chat.frequencycontrol;


import com.ccj.campus.chat.frequencycontrol.entity.dto.FrequencyRuleDTO;
import com.ccj.campus.chat.frequencycontrol.service.AbstractFrequencyService;

import java.util.List;

/**
 * @Author ccj
 * @Date 2024-07-24 14:32
 * @Description 限流工具类, 提供编程式的限流调用方法
 */
public class FrequencyControlUtil {

    /**
     * 构造器私有
     */
    private FrequencyControlUtil() {

    }

    /**
     * 单限流策略的调用方法-编程式调用
     *
     * @param strategyName  限流策略
     * @param frequencyRule 单个限流规则
     * @param supplierThrow 业务逻辑
     */
    public static <T, K extends FrequencyRuleDTO> T executeWithFrequencyRule(String strategyName, K frequencyRule, AbstractFrequencyService.SupplierThrow<T> supplierThrow) throws Throwable {
        AbstractFrequencyService<K> frequencyService = FrequencyStrategyFactory.getFrequencyService(strategyName);
        return frequencyService.executeWidthSingleRule(frequencyRule, supplierThrow);
    }

    /**
     * 多限流策略的调用方法-编程式调用
     *
     * @param strategyName   限流策略
     * @param frequencyRules 多个限流规则
     * @param supplierThrow  业务逻辑
     */
    public static  <T, K extends FrequencyRuleDTO> T executeWithFrequencyRules(String strategyName, List<K> frequencyRules, AbstractFrequencyService.SupplierThrow<T> supplierThrow) throws Throwable {
        AbstractFrequencyService<K> frequencyService = FrequencyStrategyFactory.getFrequencyService(strategyName);
        return frequencyService.executeWidthMultiRule(frequencyRules, supplierThrow);
    }


    public static  <K extends FrequencyRuleDTO> void executeWidthFrequencyRule(String strategyName, K frequencyRule, AbstractFrequencyService.RunnableWithThrow runnableWithThrow) throws Throwable {
        AbstractFrequencyService<FrequencyRuleDTO> frequencyService = FrequencyStrategyFactory.getFrequencyService(strategyName);
        frequencyService.executeWidthSingleRule(frequencyRule, () -> {
            runnableWithThrow.run();
            return null;
        });
    }

}
