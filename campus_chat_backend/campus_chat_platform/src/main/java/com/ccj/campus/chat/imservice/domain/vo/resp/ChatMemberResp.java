package com.ccj.campus.chat.imservice.domain.vo.resp;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Description: 群成员列表的成员信息
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMemberResp {
    /**
     * 用户id
     */
    private Long uid;

    /**
     * 在线状态 1在线 2离线
     *
     * @see com.ccj.campus.chat.imservice.enums.UserActiveStatusEnum
     */
    private Integer activeStatus;

    /**
     * 角色ID
     */
    private Integer roleId;

    /**
     * 最后一次上下线时间
     */
    private LocalDateTime lastOptTime;
}
