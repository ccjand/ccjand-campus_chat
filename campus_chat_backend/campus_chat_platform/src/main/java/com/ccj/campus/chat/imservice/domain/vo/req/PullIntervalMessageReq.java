package com.ccj.campus.chat.imservice.domain.vo.req;

import lombok.Data;

import java.util.List;

/**
 * @Author ccj
 * @Date 2024-07-22 14:40
 * @Description
 */
@Data
public class PullIntervalMessageReq {

    private Integer batchSize;

    private Long roomId;

    private Long lastPullMessageId;

    private Long lastPushMessageId;
}
