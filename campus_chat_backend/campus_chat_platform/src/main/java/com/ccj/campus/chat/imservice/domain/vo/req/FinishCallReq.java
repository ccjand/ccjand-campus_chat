package com.ccj.campus.chat.imservice.domain.vo.req;

import lombok.Data;

/**
 * @Author ccj
 * @Date 2024-05-25 14:23
 * @Description 结束通话请求
 */
@Data
public class FinishCallReq {

    /**
     * 呼叫方
     */
    private Long callerUId;

    /**
     * 被叫方
     */
    private Long calleeUId;


    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 房间id
     */
    private Long roomId;

    /**
     * 消息id
     */
    private Long messageId;

    /**
     * 通话时长(单位：秒)
     */
    private Long talkTime;

    /**
     * 会话token
     */
    private String sessionToken;
}
