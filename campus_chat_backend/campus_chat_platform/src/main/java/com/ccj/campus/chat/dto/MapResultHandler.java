package com.ccj.campus.chat.dto;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author ccj
 * @Date 2024-06-30 10:50
 * @Description
 */
public class MapResultHandler<K, V> implements ResultHandler<Map<K, V>> {

    private final Map<K,V> mappedResults = new HashMap<>();

    @Override
    public void handleResult(ResultContext<? extends Map<K, V>> resultContext) {
        Map map = resultContext.getResultObject();
        mappedResults.put((K) map.get("key"), (V) map.get("value"));
    }

    public Map<K,V> getMappedResults() {
        return mappedResults;
    }
}
