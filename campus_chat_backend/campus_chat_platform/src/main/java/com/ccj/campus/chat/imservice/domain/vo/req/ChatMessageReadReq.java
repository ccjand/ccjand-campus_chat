package com.ccj.campus.chat.imservice.domain.vo.req;

import com.ccj.campus.chat.dto.CursorPageBaseReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @Author ccj
 * @Date 2024-06-30 14:24
 * @Description
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatMessageReadReq extends CursorPageBaseReq {

    @NotNull
    private Long messageId;

    @NotNull
    private Integer searchType;
}
