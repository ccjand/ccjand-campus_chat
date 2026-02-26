package com.ccj.campus.chat.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CheckInTeacherClassResp implements Serializable {
    private Long classId;
    private String className;
}
