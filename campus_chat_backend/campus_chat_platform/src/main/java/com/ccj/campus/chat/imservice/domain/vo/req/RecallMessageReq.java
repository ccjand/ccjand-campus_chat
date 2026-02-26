package com.ccj.campus.chat.imservice.domain.vo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author ccj
 * @Date 2024-05-16 15:06w
 * @Description 撤回消息需要的请求体
 */
@Data
public class RecallMessageReq {

    //消息id
    @NotNull
    private Long messageId;

    //聊天室id
    @NotNull
    private Long roomId;
}
