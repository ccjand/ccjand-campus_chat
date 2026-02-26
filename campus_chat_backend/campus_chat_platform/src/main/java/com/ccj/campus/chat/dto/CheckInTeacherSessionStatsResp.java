package com.ccj.campus.chat.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CheckInTeacherSessionStatsResp implements Serializable {
    private Long sessionId;
    private Integer totalStudents;
    private Integer checkedInStudents;
    private List<ClassStats> classStats;

    @Data
    public static class ClassStats implements Serializable {
        private Long classId;
        private String className;
        private Integer totalStudents;
        private Integer checkedInStudents;
    }
}
