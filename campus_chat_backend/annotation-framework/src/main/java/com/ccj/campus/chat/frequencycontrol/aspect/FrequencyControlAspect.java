package com.ccj.campus.chat.frequencycontrol.aspect;

import com.ccj.campus.chat.frequencycontrol.FrequencyControlUtil;
import com.ccj.campus.chat.frequencycontrol.FrequencyException;
import com.ccj.campus.chat.frequencycontrol.annotation.FrequencyRule;
import com.ccj.campus.chat.frequencycontrol.entity.FrequencyStrategy;
import com.ccj.campus.chat.frequencycontrol.entity.dto.FixedWindowDto;
import com.ccj.campus.chat.frequencycontrol.entity.dto.SlidingWindowDTO;
import com.ccj.campus.chat.frequencycontrol.entity.dto.TokenBucketDTO;
import com.ccj.campus.chat.util.RequestHolder;
import com.ccj.campus.chat.util.SpElUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-07-24 11:48
 * @Description
 */
@Component
@Aspect
public class FrequencyControlAspect {

    @Around("@annotation(com.ccj.campus.chat.frequencycontrol.annotation.FrequencyRule)||@annotation(com.ccj.campus.chat.frequencycontrol.annotation.FrequencyControl))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        FrequencyRule[] frequencyRules = method.getAnnotationsByType(FrequencyRule.class);

        HashMap<String, FrequencyRule> frequencyMap = new HashMap<>();

        String key = "";
        String strategy = FrequencyStrategy.TOTAL_COUNT_IN_FIXED_TIME;
        for (int i = 0; i < frequencyRules.length; i++) {
            FrequencyRule frequencyRule = frequencyRules[i];
            //默认没有设置就是方法全限名+控制规则的排名
            String prefix = StringUtils.isNotBlank(frequencyRule.prefix()) ? frequencyRule.prefix() : method.toGenericString() + ":index:" + i;
            strategy = frequencyRule.strategy();
            switch (frequencyRule.target()) {
                case EL -> key = SpElUtils.parseSpEl(method, joinPoint.getArgs(), frequencyRule.elValue());
                case UID -> key = RequestHolder.get().getUid().toString();
            }
            frequencyMap.put(prefix + ":" + key, frequencyRule);
        }

        //将注解的配置信息封装成编程式调用需要的参数
        switch (strategy) {
            case FrequencyStrategy.SLIDING_WINDOW -> {
                //滑动窗口
                List<SlidingWindowDTO> fixedWindowDtoList = frequencyMap.entrySet().stream().map(entry -> buildSlidingWindowDto(entry.getKey(), entry.getValue())).collect(Collectors.toList());
//                return FrequencyControlUtil.executeWithFrequencyRules(strategy, fixedWindowDtoList, joinPoint::proceed);
                return FrequencyControlUtil.executeWithFrequencyRules(strategy, fixedWindowDtoList, joinPoint::proceed);
            }
            case FrequencyStrategy.TOKEN_BUCKET -> {
                //令牌桶
                List<TokenBucketDTO> tokenBucketDTOList = frequencyMap.entrySet().stream().map(entry -> buildTokenBucketDto(entry.getKey(), entry.getValue())).collect(Collectors.toList());
                return FrequencyControlUtil.executeWithFrequencyRules(strategy, tokenBucketDTOList, joinPoint::proceed);
            }
            case FrequencyStrategy.TOTAL_COUNT_IN_FIXED_TIME -> {
                //固定时间段内总次数
                List<FixedWindowDto> slidingWindowDTOList = frequencyMap.entrySet().stream().map(entry -> buildFixedWindowDto(entry.getKey(), entry.getValue())).collect(Collectors.toList());
                return FrequencyControlUtil.executeWithFrequencyRules(strategy, slidingWindowDTOList, joinPoint::proceed);
            }
            default -> throw new FrequencyException("不支持的频率控制策略");
        }
    }

    private SlidingWindowDTO buildSlidingWindowDto(String key, FrequencyRule value) {
        SlidingWindowDTO slidingWindowDTO = new SlidingWindowDTO();
        slidingWindowDTO.setPeriod(value.period());
        slidingWindowDTO.setWindowSize(value.windowSize());
        slidingWindowDTO.setKey(key);
        slidingWindowDTO.setCount(value.count());
        slidingWindowDTO.setUnit(value.unit());
        return slidingWindowDTO;
    }

    private TokenBucketDTO buildTokenBucketDto(String key, FrequencyRule value) {
        TokenBucketDTO tokenBucketDTO = new TokenBucketDTO(value.capacity(), value.refillRate());
        tokenBucketDTO.setKey(key);
        return tokenBucketDTO;
    }

    public FixedWindowDto buildFixedWindowDto(String key, FrequencyRule frequencyRule) {
        FixedWindowDto fixedWindowDto = new FixedWindowDto();
        fixedWindowDto.setTime(frequencyRule.time());
        fixedWindowDto.setCount(frequencyRule.count());
        fixedWindowDto.setKey(key);
        fixedWindowDto.setUnit(frequencyRule.unit());
        return fixedWindowDto;
    }
}
