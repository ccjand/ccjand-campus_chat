package com.ccj.campus.chat.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeacherCheckInCodeResp implements Serializable {
    private Long sessionId;
    private String code;
    private Long expireSeconds;
    private Long expireAt;
}
