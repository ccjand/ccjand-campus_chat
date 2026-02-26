package com.ccj.campus.chat.cache;

import com.ccj.campus.chat.service.impl.GroupMemberServiceImpl;
import com.ccj.campus.chat.utils.RedisKey;
import com.ccj.campus.chat.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-05-10 11:33
 * @Description 缓存所有群的所有管理员、群主的uid
 */
@Component
@RequiredArgsConstructor
public class GroupManagerCache {

    private final GroupMemberServiceImpl groupMemberDao;

    public String getKey(Long groupId) {
        return RedisKey.getKey(RedisKey.GROUP_MANAGER, groupId);
    }

    public Set<String> get(Long groupId) {
        //管理员和群主的uid
        Set<String> uidSet = RedisUtils.sGet(groupId.toString());

        if (uidSet == null || uidSet.isEmpty()) {
            //数据库载入并返回
            uidSet = refresh(groupId);
        }

        return uidSet;
    }


    public Set<String> refresh(Long groupId) {
        //管理员和群主的uid
        List<Long> uidList = groupMemberDao.getManagersOrOwner(groupId);
        Set<String> set = uidList.stream().map(Object::toString).collect(Collectors.toSet());
        String key = getKey(groupId);
        RedisUtils.sSetAndParser(key, set, Object::toString);
        return set;
    }


    public void delete(Long groupId) {
        RedisUtils.del(groupId.toString());
    }

    public void renewal(String key, long time, TimeUnit timeUnit) {
        RedisUtils.expire(key, time, timeUnit);
    }

}
