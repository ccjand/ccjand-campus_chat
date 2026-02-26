package com.ccj.campus.chat.imservice.domain.vo.resp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author ccj
 * @Date 2024-07-01 12:45
 * @Description
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupMemberChange {
    @JsonIgnore
    public final static Integer CHANGE_TYPE_ADD = 1;

    @JsonIgnore
    public final static Integer CHANGE_TYPE_REMOVE = 2;

    //房间id
    private Long roomId;

    //目标用户id
    private Long uid;

    //1:加入 2:离开
    private Integer changeType;

    /**
     * @see com.ccj.campus.chat.imservice.enums.UserActiveStatusEnum
     */
    //在线状态
    private Integer activeStatus;
}
