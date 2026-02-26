package com.ccj.campus.chat.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @Author ccj
 * @Date 2024-07-16 14:08
 * @Description
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetGroupMemberBaseReq extends PageBaseReq {

    @NotNull
    private Long roomId;
}
