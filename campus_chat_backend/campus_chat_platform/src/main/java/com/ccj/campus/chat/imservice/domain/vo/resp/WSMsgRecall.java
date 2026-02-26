package com.ccj.campus.chat.imservice.domain.vo.resp;

import lombok.Data;

@Data
public class WSMsgRecall  {
    private Long roomId;
    private Long messageId;
    private Long operatorUid;
    private String text;
}
