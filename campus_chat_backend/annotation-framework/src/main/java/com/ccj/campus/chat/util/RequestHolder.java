package com.ccj.campus.chat.util;


import com.ccj.campus.chat.frequencycontrol.entity.dto.RequestHolderInfo;

/**
 * @Author ccj
 * @Date 2024-05-04 22:14
 * @Description
 */
public class RequestHolder {

    private final static ThreadLocal<RequestHolderInfo> threadLocal = new ThreadLocal<>();

    public static void set(RequestHolderInfo requestHolderInfo) {
        threadLocal.set(requestHolderInfo);
    }


    public static RequestHolderInfo get() {
        return threadLocal.get();
    }


    public static void remove() {
        threadLocal.remove();
    }


}
