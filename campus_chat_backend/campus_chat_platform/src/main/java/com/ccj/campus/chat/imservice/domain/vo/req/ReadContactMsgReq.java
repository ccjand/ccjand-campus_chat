package com.ccj.campus.chat.imservice.domain.vo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @Author ccj
 * @Date 2024-06-30 11:45
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadContactMsgReq {

    @NotNull(message = "roomId不能为空")
    private Long roomId;
}
