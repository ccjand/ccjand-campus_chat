package com.ccj.campus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginReq {

    @NotBlank(message = "学号/工号不能为空")
    private String accountNumber;

    @NotBlank(message = "密码不能为空")
    private String password;
}