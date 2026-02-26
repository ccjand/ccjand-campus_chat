package com.ccj.campus.chat.secureinvoke.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.ccj.campus.chat.secureinvoke.dao.SecureInvokeRecordDao;
import com.ccj.campus.chat.secureinvoke.entity.SecureInvokeRecord;
import com.ccj.campus.chat.secureinvoke.entity.dto.SecureInvokeDTO;
import com.ccj.campus.chat.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-05-14 20:05
 * @Description
 */
@Slf4j
@AllArgsConstructor
public class SecureInvokeService {

    private final SecureInvokeRecordDao secureInvokeRecordDao;
    private final Executor executor;

    public static int RETRY_INTERVAL_MINUTE = 2;
    public static int RETRY_INTERVAL_SECOND = 3;


    /**
     * @param record 消息记录
     * @param async  是否异步执行
     */

    public void invoke(SecureInvokeRecord record, boolean async) {
        boolean inTransaction = TransactionSynchronizationManager.isActualTransactionActive();
        //非事务状态，直接执行，不做任何保证。
        if (!inTransaction) {
            return;
        }

        //保存数据
        secureInvokeRecordDao.save(record);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                //事务之后执行
                if (async) {
                    doAsyncInvoke(record);
                } else {
                    doInvoke(record);
                }
            }
        });
    }

    private void doInvoke(SecureInvokeRecord record) {

        try {
            SecureInvokeHolder.setInvokeState();
            SecureInvokeDTO secureInvokeDTO = record.getSecureInvokeJson();

            Class<?> beanClass = Class.forName(secureInvokeDTO.getClassName());
            Object bean = SpringUtil.getBean(beanClass);

            //解析回list
            List<String> parameterNames = JsonUtils.toList(secureInvokeDTO.getParameters(), String.class);
            //参数的真实类型
            List<Class<?>> parameterTypes = getParameterTypes(parameterNames);

            //需要被调用的那个方法
            Class<?>[] parameterTypeClasses = parameterTypes.toArray(new Class<?>[]{});
            Method method = ReflectUtil.getMethod(beanClass, secureInvokeDTO.getMethodName(), parameterTypeClasses);
            Object[] args = getArgs(secureInvokeDTO.getArgs(), parameterTypeClasses);

            //直接调用
            method.invoke(bean, args);

            //方法完整地执行成功了, 直接从本地消息表中删除即可
            secureInvokeRecordDao.removeById(record.getId());

        } catch (Throwable e) {
            log.error("SecureInvokeService doInvoke失败", e);
            //方法执行失败了, 等待下次重试
            retryRecord(record, e.getCause().getMessage());
        } finally {
            //从threadLocal中删除，本地调用已经结束了
            SecureInvokeHolder.invoked();
        }
    }

    private void retryRecord(SecureInvokeRecord record, String message) {
        //更新信息
        SecureInvokeRecord update = new SecureInvokeRecord();
        Integer retryTimes = record.getRetryTimes() + 1;
        update.setId(record.getId());
        if (record.getFailReason() != null) {
            message = record.getFailReason() + ";" + message;
        }
        update.setFailReason(message);

        if (retryTimes >= record.getMaxRetryTimes()) {
            //失败了
            update.setStatus(SecureInvokeRecord.STATUE_FAIL);
        } else {
            update.setRetryTimes(retryTimes);
            update.setNextRetryTime(getNextRetryTime(retryTimes));
        }

        secureInvokeRecordDao.updateById(update);
    }


    /**
     * 定时任务, 每5秒执行一次
     */
    @Scheduled(cron = "*/5 * * * * ?")
    public void retry() {
        List<SecureInvokeRecord> records = secureInvokeRecordDao.getWaitRetryRecord();
        records.forEach(this::doAsyncInvoke);
    }


    private LocalDateTime getNextRetryTime(Integer retryTime) {
        double waitTime = Math.pow(RETRY_INTERVAL_MINUTE, retryTime);// 3^1 -> 3^2 -> 3^3
        return LocalDateTime.now().plusMinutes((long) waitTime);
    }


    private Object[] getArgs(String argsJson, Class<?>[] parameterTypes) {
        JsonNode jsonNode = JsonUtils.toJsonNode(argsJson);

        if (parameterTypes.length != jsonNode.size()) {
            throw new RuntimeException("入参和传参个数不一致");
        }

        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            JsonNode node = jsonNode.get(i);
            Class<?> parameterClass = parameterTypes[i];
            Object value = JsonUtils.nodeToValue(node, parameterClass);
            args[i] = value;
        }
        return args;
    }


    private List<Class<?>> getParameterTypes(List<String> parameterNames) {
        return parameterNames.stream().map(paramName -> {
            try {
                return Class.forName(paramName);
            } catch (ClassNotFoundException e) {
                log.error("SecureInvokeService getParameterTypes失败: {}", e.getMessage(), e);
            }

            return null;
        }).collect(Collectors.toList());
    }

    private void doAsyncInvoke(SecureInvokeRecord record) {
        executor.execute(() -> doInvoke(record));
    }
}
