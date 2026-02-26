package com.ccj.campus.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 私聊或单聊房间表
 * </p>
 *
 * @author CCJ
 * @since 2024-05-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("room_friend")
public class RoomFriend implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 房间id
     */
    @TableField("room_id")
    private Long roomId;

    /**
     * uid1（更小的uid）
     */
    @TableField("uid")
    private Long uid1;

    /**
     * uid2（更大的uid）
     */
    @TableField("friend_uid")
    private Long uid2;

    /**
     * 房间状态 0正常 1禁用(删好友了禁用)
     */
    @TableField("delete_status")
    private Integer status;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;


}
