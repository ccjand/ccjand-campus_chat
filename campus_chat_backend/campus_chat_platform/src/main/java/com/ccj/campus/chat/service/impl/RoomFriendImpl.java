package com.ccj.campus.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccj.campus.chat.entity.RoomFriend;
import com.ccj.campus.chat.enums.DeleteStatusEnum;
import com.ccj.campus.chat.mapper.RoomFriendMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 私聊或单聊房间表 服务实现类
 * </p>
 *
 * @author CCJ
 * @since 2024-05-06
 */
@Service
public class RoomFriendImpl extends ServiceImpl<RoomFriendMapper, RoomFriend> {

    public void disableRoom(String roomKey) {
        long[] pair = parseRoomKey(roomKey);
        long uid1 = pair[0];
        long uid2 = pair[1];
        lambdaUpdate().eq(RoomFriend::getUid1, uid1)
                .eq(RoomFriend::getUid2, uid2)
                .set(RoomFriend::getStatus, DeleteStatusEnum.DELETED.getStatus())
                .update();
    }

    public void enableRoomFriend(String roomKey) {
        long[] pair = parseRoomKey(roomKey);
        long uid1 = pair[0];
        long uid2 = pair[1];
        lambdaUpdate().eq(RoomFriend::getUid1, uid1)
                .eq(RoomFriend::getUid2, uid2)
                .set(RoomFriend::getStatus, DeleteStatusEnum.NOT_DELETED.getStatus())
                .update();
    }

    public RoomFriend getByRoomKey(String roomKey) {
        long[] pair = parseRoomKey(roomKey);
        long uid1 = pair[0];
        long uid2 = pair[1];
        return lambdaQuery().eq(RoomFriend::getUid1, uid1)
                .eq(RoomFriend::getUid2, uid2)
                .one();
    }



    public RoomFriend getByRoomId(Long roomId) {
        return lambdaQuery().eq(RoomFriend::getRoomId, roomId).one();
    }

    public List<RoomFriend> getByRoomIdList(List<Long> roomIdList) {
        return lambdaQuery().in(RoomFriend::getRoomId, roomIdList)
//                .eq(RoomFriend::getStatus, DeleteStatusEnum.NOT_DELETED.getStatus())
                .list();
    }

    public void updateByRoomId(RoomFriend updateRoom) {
        lambdaUpdate().set(RoomFriend::getStatus, updateRoom.getStatus())
                .eq(RoomFriend::getRoomId, updateRoom.getRoomId())
                .update();
    }

    private long[] parseRoomKey(String roomKey) {
        if (roomKey == null || roomKey.isBlank()) {
            throw new IllegalArgumentException("roomKey is blank");
        }
        String[] parts = roomKey.split("_");
        if (parts.length != 2) {
            throw new IllegalArgumentException("invalid roomKey: " + roomKey);
        }
        long a = Long.parseLong(parts[0]);
        long b = Long.parseLong(parts[1]);
        long uid1 = Math.min(a, b);
        long uid2 = Math.max(a, b);
        return new long[]{uid1, uid2};
    }
}
