package com.ccj.campus.chat.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CreateCheckInSessionResp implements Serializable {
    private Long sessionId;
    private Long startTime;
    private Long endTime;
}
