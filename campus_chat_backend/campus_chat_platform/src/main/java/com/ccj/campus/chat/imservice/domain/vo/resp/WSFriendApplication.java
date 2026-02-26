package com.ccj.campus.chat.imservice.domain.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author ccj
 * @Date 2024-05-05 19:26
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WSFriendApplication {
    private Long uid;
    private Integer unreadCount;
}
