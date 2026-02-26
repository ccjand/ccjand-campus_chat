package com.ccj.campus.chat.redissonlock.aspect;


import com.ccj.campus.chat.redissonlock.annotation.RedissonLock;
import com.ccj.campus.chat.redissonlock.service.LockService;
import com.ccj.campus.chat.util.SpElUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @Author ccj
 * @Date 2024-04-11 16:02
 * @Description
 */
@Component
@Slf4j
@Aspect
@Order(0) //确保锁比事务先执行, 否则锁了个寂寞【分布式锁在事务之外】
public class RedissonLockAspect {

    @Autowired
    private LockService lockService;

    @Around("@annotation(redissonLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String prefixKey = getPrefixKey(method, redissonLock);
        String key = SpElUtils.parseSpEl(method, joinPoint.getArgs(), redissonLock.key());
        key = prefixKey + ":" + key;
        return lockService.executeWithLockThrows(key, redissonLock.waitTime(), redissonLock.timeUnit(), joinPoint::proceed);
    }


    private String getPrefixKey(Method method, RedissonLock redissonLock) {
        String prefix = redissonLock.prefixKey();
        if (StringUtils.isBlank(prefix)) {
            prefix = method.getDeclaringClass() + "#" + method.getName();
        }
        return prefix;
    }


}
