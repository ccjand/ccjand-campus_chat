package com.ccj.campus.chat.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeacherCheckInQrCodeResp implements Serializable {
    private Long sessionId;
    private String content;
    private String imageBase64;
    private Long expireSeconds;
    private Long expireAt;
}
