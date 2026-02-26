package com.ccj.campus.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccj.campus.chat.entity.Room;
import com.ccj.campus.chat.mapper.RoomMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author CCJ
 * @since 2024-05-06
 */
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> {

    public Room getByRoomId(Long roomId) {
        return lambdaQuery().eq(Room::getId, roomId).one();
    }


    public void refreshRoom(Long roomId, Long messageId, LocalDateTime sendTime) {
        lambdaUpdate()
                .eq(Room::getId, roomId)
                .and(wrapper -> wrapper.isNull(Room::getLastMsgId).or().lt(Room::getLastMsgId, messageId))//保证消息编号时单调递增的,假如发送了编号为39和40的消息，而40先被处理，那么后来39理应就丢弃掉(用户端显示发送失败), 不然会影响读扩散
                .set(Room::getLastMsgId, messageId)
                .set(Room::getActiveTime, sendTime)
                .update();
    }
}
