package com.ccj.campus.chat.secureinvoke.service;

/**
 * @Author ccj
 * @Date 2024-05-14 18:59
 * @Description
 */
public class SecureInvokeHolder {

    private static final ThreadLocal<Boolean> INVOKE_STATE = new ThreadLocal<>();


    /**
     * 某个方法是否正在调用中
     */
    public static boolean isInvoking() {
        return INVOKE_STATE.get() != null;
    }


    /**
     * 设置某个方法的调用状态
     */
    public static void setInvokeState() {
        INVOKE_STATE.set(Boolean.TRUE);
    }


    /**
     * 某个方法已经调用结束
     */
    public static void invoked() {
        INVOKE_STATE.remove();
    }


}
