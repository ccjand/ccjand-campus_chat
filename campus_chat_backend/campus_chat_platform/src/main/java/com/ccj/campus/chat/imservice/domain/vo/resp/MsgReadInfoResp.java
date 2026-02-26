package com.ccj.campus.chat.imservice.domain.vo.resp;

import lombok.Data;

/**
 * @Author ccj
 * @Date 2024-06-30 12:01
 * @Description
 */
@Data
public class MsgReadInfoResp {

    private Long msgId;
    private Integer readCount;
    private Integer unReadCount;
}
