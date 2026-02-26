package com.ccj.campus.chat.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CheckInStudentActiveSessionResp implements Serializable {
    private Long sessionId;
    private Long courseId;
    private String courseName;
    private String title;
    private Integer radiusMeters;
    private BigDecimal centerLatitude;
    private BigDecimal centerLongitude;
    private Long startTime;
    private Long endTime;
    private Boolean checkedIn;
    private Boolean codeEnabled;
}
