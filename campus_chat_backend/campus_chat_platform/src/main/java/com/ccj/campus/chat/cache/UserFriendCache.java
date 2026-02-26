package com.ccj.campus.chat.cache;

import com.ccj.campus.chat.entity.UserFriend;
import com.ccj.campus.chat.service.impl.UserFriendServiceImpl;
import com.ccj.campus.chat.utils.RedisKey;
import com.ccj.campus.chat.utils.RedisUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Author ccj
 * @Date 2024-06-13 23:18
 * @Description
 */
@Component
@AllArgsConstructor
public class UserFriendCache {

    private final UserFriendServiceImpl userFriendServiceImpl;

    protected String getKey(Long myUid, Long friendUid) {
        return RedisKey.getKey(RedisKey.FRIEND_KEY, myUid, friendUid);
    }


    private UserFriend load(String key, Long myUid, Long friendUid) {
        UserFriend friend = userFriendServiceImpl.getFriend(myUid, friendUid);
        if (friend != null) {
            RedisUtils.set(key, friend, getExpireMinutes(), TimeUnit.MINUTES);
        }

        return friend;
    }

    public UserFriend getFriend(Long myUid, Long friendUid) {
        String key = getKey(myUid, friendUid);
        UserFriend userFriend = RedisUtils.get(key, UserFriend.class);
        if (userFriend == null) {
            userFriend = load(key, myUid, friendUid);
        }
        return userFriend;
    }


    public void delete(Long myUid, Long friendUid) {
        String key = getKey(myUid, friendUid);
        RedisUtils.del(key);
    }

    private Long getExpireMinutes() {
        return 20L;
    }
}
