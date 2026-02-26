package com.ccj.campus.chat.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
public class StudentCheckInCodeReq implements Serializable {
    @NotBlank(message = "签到码不能为空")
    @Pattern(regexp = "^\\d{4}$", message = "签到码格式不正确")
    private String code;
}
