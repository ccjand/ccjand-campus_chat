package com.ccj.campus.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccj.campus.chat.entity.RoomGroup;
import com.ccj.campus.chat.enums.DeleteStatusEnum;
import com.ccj.campus.chat.mapper.RoomGroupMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 群房间表 服务实现类
 * </p>
 *
 * @author CCJ
 * @since 2024-05-06
 */
@Service
public class RoomGroupServiceImpl extends ServiceImpl<RoomGroupMapper, RoomGroup> {

    public RoomGroup getByRoomId(Long roomId) {
        return lambdaQuery().eq(RoomGroup::getRoomId, roomId).one();
    }

    public List<RoomGroup> getRoomGroupByRoomIds(List<Long> roomList) {
        return lambdaQuery()
                .in(RoomGroup::getRoomId, roomList)
                .eq(RoomGroup::getDeleteStatus, DeleteStatusEnum.NOT_DELETED.getStatus())
                .list();

    }

    public void updateAvatarByRoomId(Long roomId, String objectPath) {
        lambdaUpdate().eq(RoomGroup::getRoomId, roomId)
                .set(RoomGroup::getAvatar, objectPath)
                .update();
    }
}
