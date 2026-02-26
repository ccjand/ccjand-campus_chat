package com.ccj.campus.chat.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CheckInStudentHistoryCourseResp implements Serializable {
    private Long courseId;
    private String courseName;
    private List<Record> records;

    @Data
    public static class Record implements Serializable {
        private Long checkInId;
        private Long sessionId;
        private String sessionTitle;
        private Long startTime;
        private Long endTime;
        private Boolean checkedIn;
        private Long checkInTime;
    }
}
