package com.ccj.campus.chat.imservice.domain.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WSMemberChange {
    public static final Integer CHANGE_TYPE_ADD = 1;
    public static final Integer CHANGE_TYPE_REMOVE = 2;

    /**
     * 群组id
     */
    private Long roomId;

    /**
     * 变动uid集合
     */
    private Long uid;

    /**
     * 变动类型 1加入群组 2移除群组
     */
    private Integer changeType;
    /**
     * 在线状态 1在线 2离线
     *
     * @see com.ccj.campus.chat.imservice.enums.UserActiveStatusEnum
     */
    private Integer activeStatus;

    /**
     * 最后一次上下线时间
     */
    private LocalDateTime lastOptTime;
}
