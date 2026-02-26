package com.ccj.campus.chat.cache;

import com.ccj.campus.chat.entity.RoomGroup;
import com.ccj.campus.chat.service.impl.RoomGroupServiceImpl;
import com.ccj.campus.chat.utils.RedisKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-05-09 16:51
 * @Description
 */
@Component
public class RoomGroupCache extends AbstractRedisStringCache<Long, RoomGroup> {

    @Autowired
    private RoomGroupServiceImpl roomGroupServiceImpl;

    @Override
    protected String getKey(Long roomId) {
        return RedisKey.getKey(RedisKey.ROOM_GROUP_INFO, roomId);
    }

    @Override
    protected Map<Long, RoomGroup> load(List<Long> roomIdList) {

        List<RoomGroup> groups = roomGroupServiceImpl.getRoomGroupByRoomIds(roomIdList);
        return groups.stream().collect(Collectors.toMap(RoomGroup::getRoomId, group -> group));
    }

    @Override
    protected Long getExpireSeconds() {
        return 10 * 60L;
    }


}
