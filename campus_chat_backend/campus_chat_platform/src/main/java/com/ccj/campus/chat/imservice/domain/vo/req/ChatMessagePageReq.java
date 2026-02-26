package com.ccj.campus.chat.imservice.domain.vo.req;

import com.ccj.campus.chat.dto.CursorPageBaseReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @Author ccj
 * @Date 2024-05-15 20:05
 * @Description 聊天消息分页请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatMessagePageReq extends CursorPageBaseReq {

    /**
     * 房间id
     */
    @NotNull
    private Long roomId;
}
