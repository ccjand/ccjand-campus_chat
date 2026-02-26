package com.ccj.campus.chat.secureinvoke.annotation;

import org.springframework.lang.Nullable;

import java.util.concurrent.Executor;

/**
 * @Author ccj
 * @Date 2024-05-09 10:39
 * @Description
 */
public interface SecureInvokeConfigurer {


    /**
     * 返回一个线程池
     *
     * @return 默认返回空，让子类自己实现
     */
    @Nullable
    default Executor getSecureInvokeExecutor() {
        return null;
    }
}
