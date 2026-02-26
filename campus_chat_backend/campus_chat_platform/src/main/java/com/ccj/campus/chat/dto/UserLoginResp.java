package com.ccj.campus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginResp {
    private Long uid;
    private String accountNumber;
    private String avatar;
    private String token;
    private String fullName;

    //角色 0-管理员 1-学生 2老师
    private Integer role;

    private String department;

    private String className;
}
