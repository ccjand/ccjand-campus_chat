package com.ccj.campus.chat.cache;

import com.ccj.campus.chat.utils.RedisKey;
import com.ccj.campus.chat.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-05-06 17:39
 * @Description 用户相关信息的缓存【redis】
 */
@Component
@RequiredArgsConstructor
public class UserCache {

    /**
     * 获取用户们最新的修改时间戳
     */
    public List<Long> getUserLastModifyTimeList(List<Long> uidList) {
        //组装redis的key
        List<String> keys = uidList.stream().map(uid -> RedisKey.getKey(RedisKey.USER_MODIFY_TIME, uid)).collect(Collectors.toList());
        return RedisUtils.mget(keys, Long.class);
    }


    /**
     * 刷新用户修改时间戳
     */
    public void refreshUserModifyTime(Long uid) {
        String key = RedisKey.getKey(RedisKey.USER_MODIFY_TIME, uid);
        RedisUtils.set(key, new Date().getTime(), 10, TimeUnit.SECONDS);
    }


    public void userInfoChange(Long uid) {
        delUserInfo(uid);
        //刷新修改时间戳
        refreshUserModifyTime(uid);
    }

    public void delUserInfo(Long uid) {
        String key = RedisKey.getKey(RedisKey.USER_INFO, uid);
        RedisUtils.del(key);
    }
}
