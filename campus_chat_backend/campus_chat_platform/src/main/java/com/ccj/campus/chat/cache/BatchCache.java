package com.ccj.campus.chat.cache;

import java.util.List;
import java.util.Map;

/**
 * @Author ccj
 * @Date 2024-04-17 23:11
 * @Description 批量缓存框架接口 get or load
 */
public interface BatchCache<IN, OUT> {

    /**
     * 获取单个缓存
     */
    OUT get(IN req);

    /**
     * 批量获取缓存
     */
    Map<IN, OUT> getBatch(List<IN> req);


    /**
     * 删除缓存
     */
    void delete(IN req);

    /**
     * 批量删除缓存
     */
    void deleteBatch(List<IN> req);
}
