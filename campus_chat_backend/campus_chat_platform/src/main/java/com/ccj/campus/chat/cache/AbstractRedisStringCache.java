package com.ccj.campus.chat.cache;

import cn.hutool.core.collection.CollectionUtil;
import com.ccj.campus.chat.config.ThreadPoolConfig;
import com.ccj.campus.chat.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-04-17 23:15
 * @Description redis string类型的批量缓存框架【in和out不支持嵌套类型】
 */
public abstract class AbstractRedisStringCache<IN, OUT> implements BatchCache<IN, OUT> {
    private final Class<OUT> outClass;

    @Resource
    @Qualifier(ThreadPoolConfig.CAMPUS_CHAT_EXECUTOR)
    private ThreadPoolTaskExecutor executor;

    @SuppressWarnings("unchecked")
    protected AbstractRedisStringCache() {
        //获取（带泛型类型）父类
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        outClass = (Class<OUT>) type.getActualTypeArguments()[1];
    }

    public <T> T submitTask(Callable<T> callable) {
        Future<T> submit = executor.submit(callable);
        try {
            return submit.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }


    public void executeTask(Runnable runnable) {
        executor.execute(runnable);
    }


    public void refresh(IN req, OUT out) {
        String key = getKey(req);
        if (null == out) {
            //延长时间
            RedisUtils.expire(key, this.getExpireSeconds(), TimeUnit.SECONDS);
        } else {
            //重新设置缓存
            RedisUtils.set(key, out, this.getExpireSeconds(), TimeUnit.SECONDS);
        }
    }

    protected abstract String getKey(IN req);

    protected abstract Map<IN, OUT> load(List<IN> req);

    protected abstract Long getExpireSeconds();

    @Override
    public OUT get(IN req) {
        return getBatch(Collections.singletonList(req)).get(req);
    }

    @Override
    public Map<IN, OUT> getBatch(List<IN> req) {
        if (CollectionUtil.isEmpty(req)) {
            return new HashMap<>();
        }

        //去重
        req = req.stream().distinct().collect(Collectors.toList());
        //组装批量查询的key
        List<String> keys = req.stream().map(this::getKey).collect(Collectors.toList());
        //批量查 redis
        List<OUT> values = RedisUtils.mget(keys, outClass);

        //计算差集
        ArrayList<IN> loadReqs = new ArrayList<>();//需要去重新加载的缓存
        for (int i = 0; i < values.size(); i++) {
            //哪个缓存为空, 就需要去加载
            if (values.get(i) == null) {
                loadReqs.add(req.get(i));
            }
        }

        Map<IN, OUT> load = new HashMap<>();
        //有差集就去更新
        if (!loadReqs.isEmpty()) {
            //数据库里获取最新数据
            load = load(loadReqs);
            //调整格式，写进缓存
            Map<String, OUT> loadData = load.entrySet().stream()
                    .map(entry -> Pair.of(getKey(entry.getKey()), entry.getValue()))
                    .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

            RedisUtils.mset(loadData, getExpireSeconds());
        }

        //组装最后的结果
        Map<IN, OUT> result = new HashMap<>();
        for (int i = 0; i < req.size(); i++) {
            IN in = req.get(i);
            OUT out = Optional.ofNullable(values.get(i)).orElse(load.get(in));
            result.put(in, out);
        }
        return result;
    }

    @Override
    public void delete(IN req) {
        deleteBatch(Collections.singletonList(req));
    }

    @Override
    public void deleteBatch(List<IN> req) {
        List<String> list = req.stream().map(this::getKey).collect(Collectors.toList());
        RedisUtils.del(list);
    }
}
