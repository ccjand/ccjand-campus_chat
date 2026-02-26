package com.ccj.campus.chat.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StudentCheckInResp implements Serializable {
    private Long checkInId;
    private Long sessionId;
    private Double distanceMeters;
    private Long checkInTime;
}
