package com.ccj.campus.chat.cache;

import com.ccj.campus.chat.entity.RoomFriend;
import com.ccj.campus.chat.service.impl.RoomFriendImpl;
import com.ccj.campus.chat.utils.RedisKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-05-09 17:13
 * @Description
 */
@Component
@RequiredArgsConstructor
public class RoomFriendCache extends AbstractRedisStringCache<Long, RoomFriend> {

    private final RoomFriendImpl roomFriendImpl;

    @Override
    protected String getKey(Long roomId) {
        return RedisKey.getKey(RedisKey.ROOM_FRIEND_INFO, roomId);
    }

    @Override
    protected Map<Long, RoomFriend> load(List<Long> roomIdList) {
        List<RoomFriend> roomFriends = roomFriendImpl.getByRoomIdList(roomIdList);
        return roomFriends.stream().collect(Collectors.toMap(RoomFriend::getRoomId, roomFriend -> roomFriend));
    }

    @Override
    protected Long getExpireSeconds() {
        return 10 * 60L;
    }
}
