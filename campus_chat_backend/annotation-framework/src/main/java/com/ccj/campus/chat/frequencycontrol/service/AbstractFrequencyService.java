package com.ccj.campus.chat.frequencycontrol.service;


import com.ccj.campus.chat.frequencycontrol.FrequencyException;
import com.ccj.campus.chat.frequencycontrol.FrequencyStrategyFactory;
import com.ccj.campus.chat.frequencycontrol.entity.dto.FrequencyRuleDTO;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-07-24 14:38
 * @Description 抽象的频率控制服务
 */
@Slf4j
public abstract class AbstractFrequencyService<K extends FrequencyRuleDTO> {

    /**
     * 策略的名称
     */
    public abstract String getStrategyName();

    /**
     * 判断是否达到阈值, 每个子类都可以自定义自己的限流逻辑判断
     *
     * @param ruleMap 限流规则
     * @return true:方法被限流了  false:方法正常通过
     */
    public abstract boolean reachedThreshold(Map<String, K> ruleMap);

    /**
     * 增加限流统计次数 子类实现 每个子类都可以自定义自己的限流统计信息增加的逻辑
     */
    public abstract void increaseFrequencyCount(Map<String, K> ruleMap);

    @PostConstruct
    public void register() {
        FrequencyStrategyFactory.register(getStrategyName(), this);
    }


    /**
     * @param ruleMap       频控规则, Map中的Key-对应redis的单个频控的Key
     *                      Map中的Value-对应redis的单个频控的Key限制的Value。
     *                      使用map是因为有可能同一个限流策略有多个限流规则, 如:使用多个相同注解
     * @param supplierThrow 自定义的业务逻辑
     */
    public <T> T executeWithStrategyMap(Map<String, K> ruleMap, SupplierThrow<T> supplierThrow) throws Throwable {
        if (reachedThreshold(ruleMap)) {
            throw new FrequencyException("操作频繁,请休息一下吧~");
        }
        try {
            //执行业务逻辑
            return supplierThrow.get();
        } finally {
            //不管成功还是失败，都增加次数
            increaseFrequencyCount(ruleMap);
        }
    }

    /**
     * 单个频控规则 -- 编程式注解
     *
     * @param frequencyRule 频控规则
     * @param supplierThrow 业务逻辑
     */
    public <T> T executeWidthSingleRule(K frequencyRule, SupplierThrow<T> supplierThrow) throws Throwable {
        return executeWidthMultiRule(Collections.singletonList(frequencyRule), supplierThrow);
    }


    /**
     * 多个频控规则 -- 编程式注解
     *
     * @param frequencyRules 多个频控规则
     * @param supplierThrow  业务逻辑
     */
    public <T> T executeWidthMultiRule(List<K> frequencyRules, SupplierThrow<T> supplierThrow) throws Throwable {
        boolean existRulesNullKey = frequencyRules.stream().anyMatch(frequencyRule -> Objects.isNull(frequencyRule.getKey()));
        if (existRulesNullKey) {
            throw new RuntimeException("频控规则的key不能为空");
        }

        Map<String, K> ruleMap = frequencyRules.stream().collect(Collectors.toMap(FrequencyRuleDTO::getKey, frequencyRule -> frequencyRule));
        return executeWithStrategyMap(ruleMap, supplierThrow);
    }


    @FunctionalInterface
    public interface SupplierThrow<T> {
        T get() throws Throwable;
    }

    public interface RunnableWithThrow {
        void run() throws Throwable;
    }
}
