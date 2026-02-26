package com.ccj.campus.chat.aspect;

import com.ccj.campus.chat.util.RequestHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${campus-chat.request-log.enabled:false}")
    private boolean logEnabled;

    @Pointcut("execution(* com.ccj.campus.chat.controller..*.*(..))")
    public void controllerLog() {
    }

    @Around("controllerLog()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!logEnabled) {
            return joinPoint.proceed();
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            if (request != null) {
                List<Object> logArgs = new ArrayList<>();
                for (Object arg : joinPoint.getArgs()) {
                    if (isSerializable(arg)) {
                        logArgs.add(arg);
                    }
                }
                String argsJson = "";
                try {
                    argsJson = objectMapper.writeValueAsString(logArgs);
                } catch (Exception e) {
                    argsJson = "Serialization Error";
                }
                
                String token = request.getHeader("Authorization");
                Long uid = null;
                try {
                    uid = RequestHolder.get().getUid();
                } catch (Exception ignored) {}

                log.info(">>> Request: {} {} | UID: {} | Token: {} | Args: {}", 
                        request.getMethod(), 
                        request.getRequestURI(), 
                        uid, 
                        token, 
                        argsJson);
            }
            result = joinPoint.proceed();

            String resultJson = "";
            try {
                if (result != null) {
                    resultJson = objectMapper.writeValueAsString(result);
                } else {
                    resultJson = "null";
                }
            } catch (Exception e) {
                resultJson = "Serialization Error";
            }

            log.info("<<< Response: {} Result: {}", request != null ? request.getRequestURI() : "", resultJson);
            return result;
        } catch (Throwable e) {
            log.error("<<< Error: {} Message: {}", request != null ? request.getRequestURI() : "", e.getMessage());
            throw e;
        } finally {
            log.info("--- Time: {} ms", System.currentTimeMillis() - startTime);
        }
    }

    private boolean isSerializable(Object obj) {
        if (obj == null) {
            return true;
        }
        return !(obj instanceof ServletRequest) &&
                !(obj instanceof ServletResponse) &&
                !(obj instanceof MultipartFile);
    }
}
