package com.ccj.campus.chat.imservice.domain.vo.resp;

import lombok.Data;

/**
 * @Author ccj
 * @Date 2024-05-25 14:37
 * @Description
 */
@Data
public class FinishCallResp {
    private Long callerUId;

    private Long calleeUId;

    private Long messageId;

    private Long roomId;
}