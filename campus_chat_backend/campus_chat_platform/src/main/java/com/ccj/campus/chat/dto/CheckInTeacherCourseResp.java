package com.ccj.campus.chat.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CheckInTeacherCourseResp implements Serializable {
    private Long courseId;
    private String courseName;
}
