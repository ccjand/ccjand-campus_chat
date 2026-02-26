package com.ccj.campus.chat.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class StudentCheckInQrCodeReq implements Serializable {
    @NotBlank(message = "二维码内容不能为空")
    private String content;
}
