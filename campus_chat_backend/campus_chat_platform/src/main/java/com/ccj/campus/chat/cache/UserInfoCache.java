package com.ccj.campus.chat.cache;

import com.ccj.campus.chat.dao.UserDao;
import com.ccj.campus.chat.entity.Users;
import com.ccj.campus.chat.utils.RedisKey;
import com.ccj.campus.chat.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-05-06 18:13
 * @Description
 */
@Component
@RequiredArgsConstructor
public class UserInfoCache extends AbstractRedisStringCache<Long, Users> {

    private final UserDao userDao;

    @Override
    public Map<Long, Users> getBatch(List<Long> req) {
        Map<Long, Users> result = super.getBatch(req);
        if (req == null || req.isEmpty()) {
            return result;
        }

        List<Long> needReload = req.stream()
                .filter(Objects::nonNull)
                .distinct()
                .filter(uid -> {
                    Users u = result.get(uid);
                    if (u == null) return true;
                    String avatar = u.getAvatar();
                    return avatar == null || avatar.isBlank();
                })
                .collect(Collectors.toList());

        if (needReload.isEmpty()) {
            return result;
        }

        Map<Long, Users> loaded = load(needReload);
        if (loaded == null || loaded.isEmpty()) {
            return result;
        }

        Map<String, Users> loadData = loaded.entrySet().stream()
                .collect(Collectors.toMap(e -> getKey(e.getKey()), Map.Entry::getValue));
        RedisUtils.mset(loadData, getExpireSeconds());

        Map<Long, Users> merged = new HashMap<>(result.size() + loaded.size());
        merged.putAll(result);
        merged.putAll(loaded);
        return merged;
    }

    @Override
    protected String getKey(Long uid) {
        return RedisKey.getKey(RedisKey.USER_INFO, uid);
    }

    @Override
    protected Map<Long, Users> load(List<Long> uidList) {
        List<Users> users = userDao.listByIds(uidList);
        return users.stream().collect(Collectors.toMap(Users::getId, Function.identity()));
    }

    @Override
    protected Long getExpireSeconds() {
        //10分钟
        return 10 * 60L;
    }

    public void online(Long uid) {
        RedisUtils.executeScript("/lua/updateUserOnlineStatus.lua", Collections.emptyList(), uid.toString(), "1");
    }
}
