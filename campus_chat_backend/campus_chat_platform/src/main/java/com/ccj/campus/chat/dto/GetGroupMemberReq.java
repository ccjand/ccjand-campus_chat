package com.ccj.campus.chat.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @Author ccj
 * @Date 2024-06-30 19:29
 * @Description
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetGroupMemberReq extends CursorPageBaseReq {

    @NotNull
    private Long roomId;
}
