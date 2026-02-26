package com.ccj.campus.chat.operatelog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author ccj
 * @Date 2024-07-05 16:44
 * @Description 群聊活跃度统计（用户最活跃的前10个群聊）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupChatActivityInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户uid
     */
    private Long uid;

    /**
     * 用户在这个群聊里面发了消息了
     */
    private Long roomId;

    /**
     * 用户发送消息的时间
     */
    private Long sendTime;
}
