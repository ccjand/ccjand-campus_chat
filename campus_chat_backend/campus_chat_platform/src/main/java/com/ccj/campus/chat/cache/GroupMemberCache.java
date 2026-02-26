package com.ccj.campus.chat.cache;

import com.ccj.campus.chat.config.ThreadPoolConfig;
import com.ccj.campus.chat.entity.Room;
import com.ccj.campus.chat.entity.RoomGroup;
import com.ccj.campus.chat.enums.RoomTypeEnum;
import com.ccj.campus.chat.service.impl.GroupMemberServiceImpl;
import com.ccj.campus.chat.util.JsonUtils;
import com.ccj.campus.chat.utils.AssertUtil;
import com.ccj.campus.chat.utils.RedisKey;
import com.ccj.campus.chat.utils.RedisUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ccj.campus.chat.utils.RedisKey.GROUP_MEMBER_LOCK;


/**
 * @Author ccj
 * @Date 2024-05-09 16:38
 * @Description
 */
@Component
@Slf4j
public class GroupMemberCache {

    @Autowired
    private RoomGroupCache roomGroupCache;
    @Autowired
    private GroupMemberServiceImpl groupMemberDao;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RoomCache roomCache;

    @Autowired
    @Qualifier(ThreadPoolConfig.REBUILD_CACHE_EXECUTOR)
    private ThreadPoolTaskExecutor executor;


    /**
     * 查询在群里面的指定用户uid
     */
    public Set<Long> findUidInGroup(Long roomId, List<Long> targetUidList) {
        RoomGroup roomGroup = roomGroupCache.get(roomId);
        ObjectMapper objectMapper = new ObjectMapper();
        String findUidListJson;
        try {
            String uidListJson = objectMapper.writeValueAsString(targetUidList);
            findUidListJson = RedisUtils.executeScript("/lua/findUidInGroup.lua", Collections.emptyList(), "5", uidListJson);
        } catch (Exception e) {
            log.error("findUidInGroup error", e);
            return Collections.emptySet();
        }

        if (findUidListJson == null) {
            //缓存失效了, 重新构建缓存
            List<Long> membersUidFromDB = groupMemberDao.getMembersUid(roomGroup.getId());
            this.rebuildGroupMemberUid(roomId, membersUidFromDB);
            Set<Long> uidSet = new HashSet<>(membersUidFromDB);
            HashSet<Long> find = new HashSet<>();
            for (Long uid : targetUidList) {
                if (uidSet.contains(uid)) {
                    find.add(uid);
                }
            }

            return find;
        }

        return JsonUtils.toSet(findUidListJson, Long.class);
    }


    /**
     * 获取全部的群成员uid
     */
    public Set<Long> getGroupMember(Long roomId) {
        Room room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "房间不存在");
        if (RoomTypeEnum.isSingleRoom(room.getType())) {
            return Collections.emptySet();
        }

        String key = getKey(roomId);

        RoomGroup roomGroup = roomGroupCache.get(roomId);
        Set<String> uidStr = RedisUtils.zRange(key, 0, -1);
        if (uidStr == null || uidStr.isEmpty()) {
            //缓存失效了
            List<Long> membersUid = groupMemberDao.getMembersUid(roomGroup.getId());
            //异步 重新构建缓存
            //弄一个互斥锁异步去构建缓存, 避免缓存构建时间长,且有其他线程也在构建
            rebuildGroupMemberUid(roomId, membersUid);
            return new HashSet<>(membersUid);
        }

        return uidStr.stream().map(Long::valueOf).collect(Collectors.toSet());
    }

    /**
     * 获取前n个活跃用户，如果不满n个就全部返回
     *
     * @param roomId      房间id
     * @param minActivity 最小活跃度
     * @param limitCount  前n个
     */
    public Set<Long> getActiveGroupMemberTopN(Long roomId, int minActivity, long limitCount) {
        RoomGroup roomGroup = roomGroupCache.get(roomId);
        if (roomGroup == null) {
            return Collections.emptySet();
        }

        String uidJson = RedisUtils.executeScript("/lua/getGroupActivityUserTopN.lua", Collections.emptyList(),
                String.valueOf(roomId), String.valueOf(minActivity), String.valueOf(limitCount));

        if (StringUtils.isBlank(uidJson)) {
            return Collections.emptySet();
        }

        if (!"{}".equals(uidJson)) {
            Set<Long> uidSet = JsonUtils.toSet(uidJson, Long.class);
            if (uidSet != null && uidSet.size() > 0) {
                return uidSet;
            }
        }

        //这个群很久没有发言了,没有缓存, 直接从数据库获全部然后返回前n个就行了
        List<Long> uidListFromDB = groupMemberDao.getMembersUid(roomGroup.getId());

        if (uidListFromDB == null || uidListFromDB.isEmpty()) {
            return Collections.emptySet();
        }

        rebuildGroupMemberUid(roomId, uidListFromDB);

        return uidListFromDB.stream().limit(limitCount).collect(Collectors.toSet());
    }

    public void rebuildGroupMemberUid(Long roomId, List<Long> uidList) {
        Room room = roomCache.get(roomId);
        if (RoomTypeEnum.isSingleRoom(room.getType())) {
            return;
        }
        //弄一个互斥锁异步去构建缓存, 避免缓存构建时间长,且有其他线程也在构建
        try {
            //开一个子线程重新构建缓存
            executor.submit(() -> {
                RLock lock = redissonClient.getLock(GROUP_MEMBER_LOCK);
                try {
                    if (lock.tryLock()) {
                        String key = getKey(roomId);
                        List<Long> uidListFromDB = uidList;
                        if (uidListFromDB == null || uidListFromDB.isEmpty()) {
                            uidListFromDB = groupMemberDao.getMembersUid(roomId);
                        }

                        //如果还是没有数据, 可能群聊已经解散了
                        if (uidListFromDB != null && !uidListFromDB.isEmpty()) {
                            Set<ZSetOperations.TypedTuple<String>> typedTuples = uidListFromDB.stream().map(uid -> ZSetOperations.TypedTuple.of(uid.toString(), 0.0)).collect(Collectors.toSet());
                            RedisUtils.zAdd(key, typedTuples);
                            renewal(key, expireTime(), TimeUnit.MINUTES);
                        }
                    }
                } catch (Exception e) {
                    Thread.currentThread().interrupt(); // 中断线程
                    log.error("重新构建群成员失败, roomId:{}, 原因:{}", roomId, e.getMessage(), e);
                    throw e;
                } finally {
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            });
        } catch (Exception e) {
            log.error("重新构建群成员活跃度失败, 原因:{}", e.getMessage(), e);
            throw e;
        }
    }


    public int getOnlineGroupMemberCount(Long roomId) {
        //查处所有的群成员uid
        String onlineGroupMemberCountStr = RedisUtils.executeScript("/lua/getOnlineUserCountByCondition.lua", Collections.emptyList(), roomId.toString());
        int onlineGroupMemberCount = Integer.parseInt(onlineGroupMemberCountStr);
        String key = getKey(roomId);
        if (onlineGroupMemberCount == -1) {
            //缓存已经失效了去数据库查
            RoomGroup roomGroup = roomGroupCache.get(roomId);
            List<Long> membersUid = groupMemberDao.getMembersUid(roomGroup.getId());
            //重新构建缓存
            Set<ZSetOperations.TypedTuple<String>> typedTuples = membersUid.stream().map(uid -> ZSetOperations.TypedTuple.of(uid.toString(), 0.0)).collect(Collectors.toSet());
            RedisUtils.zAdd(key, typedTuples);
            renewal(key, expireTime(), TimeUnit.MINUTES);
            onlineGroupMemberCountStr = RedisUtils.executeScript("/lua/getOnlineUserCountByCondition.lua", Collections.emptyList(), roomId.toString());
            onlineGroupMemberCount = Integer.parseInt(onlineGroupMemberCountStr);
            return onlineGroupMemberCount;
        }

        //续期
        renewal(key, expireTime(), TimeUnit.MINUTES);
        return onlineGroupMemberCount;
    }


    public boolean hasUser(Long roomId, Long uid) {
        Set<Long> members = getGroupMember(roomId);
        return members.contains(uid);
    }


    public boolean isMember(Long uid, Long roomId) {
        Set<Long> members = getGroupMember(roomId);
        return members.contains(uid);
    }


    public void push(Long roomId, Long uid) {
        String key = getKey(roomId);
        RedisUtils.zAdd(key, uid.toString(), 0.0);
    }

    public void pushAll(Long roomId, List<Long> uidList) {
        String key = getKey(roomId);
        Set<ZSetOperations.TypedTuple<String>> typedTuples = uidList.stream().map(uid -> ZSetOperations.TypedTuple.of(uid.toString(), 0.0)).collect(Collectors.toSet());
        RedisUtils.zAdd(key, typedTuples);
        renewal(key, expireTime(), TimeUnit.MINUTES);
    }

    public void remove(Long roomId) {
        String key = getKey(roomId);
        RedisUtils.del(key);
    }

    public void removeItem(Long roomId, Long uid) {
        String key = getKey(roomId);
        RedisUtils.hdel(key, uid.toString());
    }

    public void renewal(String key, long time, TimeUnit timeUnit) {
        RedisUtils.expire(key, time, timeUnit);
    }

    public String getKey(Long roomId) {
        return RedisKey.getKey(RedisKey.GROUP_MEMBER, roomId);
    }

    public long expireTime() {
        return 60;//60分钟
    }


    /**
     * 具体某个群的人数
     */
    public Integer getGroupMembersCount(Long roomId) {
        return getGroupMembersCount(List.of(roomId)).get(roomId);
    }

    /**
     * 每个群的人数
     * key: roomId
     * value: 房间人数
     */
    public Map<Long, Integer> getGroupMembersCount(List<Long> roomIdList) {
        if (roomIdList == null || roomIdList.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String roomIdListJson = objectMapper.writeValueAsString(roomIdList);
            String resultJson = RedisUtils.executeScript("lua/getMultiRoomMemberCount.lua", Collections.emptyList(), roomIdListJson);
            HashMap<Long, Integer> roomCountMap = new HashMap<>();

            if (resultJson != null) {
                JsonNode jsonNode = objectMapper.readTree(resultJson);
                jsonNode.fields().forEachRemaining(entry -> {
                    Long key = Long.valueOf(entry.getKey());
                    Integer count = Integer.valueOf(entry.getValue().asText());
                    roomCountMap.put(key, count);
                });
            }

            System.out.println("roomCountMap = " + roomCountMap);

            List<Long> needLoadRoomId = roomIdList.stream().filter(roomId -> !roomCountMap.containsKey(roomId)).collect(Collectors.toList());

            if (needLoadRoomId.isEmpty()) {
                return roomCountMap;
            }

            List<Long> groupRoomId = roomGroupCache.getBatch(needLoadRoomId).values()
                    .stream().filter(Objects::nonNull).map(RoomGroup::getId).collect(Collectors.toList());

            //从数据库加载房间成员人数
            Map<Long, Integer> members = groupMemberDao.getRoomMemberById(groupRoomId);
            roomCountMap.putAll(members);

            //异步 添加到缓存, 加载需要的就行了
            //重新构建缓存
            //数据库查询全部用户uid
            needLoadRoomId.forEach(roomId -> {
                this.rebuildGroupMemberUid(roomId, null);
            });

            return roomCountMap;
        } catch (Exception e) {
            log.error("获取群成员人数失败, 原因:{}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
