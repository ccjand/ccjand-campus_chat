package com.ccj.campus.chat.cache;

import com.ccj.campus.chat.entity.Room;
import com.ccj.campus.chat.service.impl.RoomServiceImpl;
import com.ccj.campus.chat.utils.RedisKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-05-09 13:51
 * @Description
 */
@Component
@RequiredArgsConstructor
public class RoomCache extends AbstractRedisStringCache<Long, Room> {

    private final RoomServiceImpl roomServiceImpl;

    @Override
    protected String getKey(Long roomId) {
        return RedisKey.getKey(RedisKey.ROOM_INFO, roomId);
    }

    @Override
    protected Map<Long, Room> load(List<Long> roomIdList) {
        List<Room> rooms = roomServiceImpl.listByIds(roomIdList);
        return rooms.stream().collect(Collectors.toMap(Room::getId, Function.identity()));
    }

    @Override
    protected Long getExpireSeconds() {
        return 5 * 60L;
    }

    public void refreshRoom(Room room, Long messageId) {
        roomServiceImpl.refreshRoom(room.getId(), messageId, room.getActiveTime());
        //更新缓存的最新数据
        this.refresh(room.getId(), room);
    }

}
