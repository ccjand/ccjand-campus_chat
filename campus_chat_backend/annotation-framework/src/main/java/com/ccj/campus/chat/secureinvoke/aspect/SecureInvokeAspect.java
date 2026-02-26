package com.ccj.campus.chat.secureinvoke.aspect;


import cn.hutool.core.date.DateUtil;
import com.ccj.campus.chat.secureinvoke.annotation.SecureInvoke;
import com.ccj.campus.chat.secureinvoke.entity.SecureInvokeRecord;
import com.ccj.campus.chat.secureinvoke.entity.dto.SecureInvokeDTO;
import com.ccj.campus.chat.secureinvoke.service.SecureInvokeHolder;
import com.ccj.campus.chat.secureinvoke.service.SecureInvokeService;
import com.ccj.campus.chat.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-05-14 18:49
 * @Description SecureInvoke注解切面，利用本地消息表的方式确保安全执行被该注解方法
 */
@Slf4j
@Component
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 1)//确保最先执行
public class SecureInvokeAspect {

    @Autowired
    private SecureInvokeService secureInvokeService;

    @Around("@annotation(secureInvoke)")
    public Object aroundSecureInvoke(ProceedingJoinPoint joinPoint, SecureInvoke secureInvoke) throws Throwable {
        //非事务状态下直接执行，不做任何保证, 某个方法正在调用中也是不做处理，直接执行
        boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        if (SecureInvokeHolder.isInvoking() || !transactionActive) {
            return joinPoint.proceed();
        }


        //拿到方法签名
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        //拿到方法的参数列表
        List<String> parameterNames = Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.toList());

        SecureInvokeDTO secureInvokeDTO = SecureInvokeDTO.builder()
                .className(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(JsonUtils.toStr(parameterNames))
                .args(JsonUtils.toStr(joinPoint.getArgs()))
                .build();


        //存入消息表
        SecureInvokeRecord record = SecureInvokeRecord.builder()
                .secureInvokeJson(secureInvokeDTO)
                .maxRetryTimes(secureInvoke.maxRetryTime())
                .nextRetryTime(LocalDateTime.now().plusSeconds(SecureInvokeService.RETRY_INTERVAL_SECOND))
                .build();

        boolean async = secureInvoke.async();
        secureInvokeService.invoke(record, async);
        return null;
    }


}
