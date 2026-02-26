package com.ccj.campus.chat.service;

import com.ccj.campus.chat.dto.UserLoginReq;
import com.ccj.campus.chat.dto.UserLoginResp;

import javax.servlet.http.HttpServletRequest;

public interface UsersService {

    Long getValidUid(String token);

    UserLoginResp login(UserLoginReq loginReq, HttpServletRequest request);

    void logout(Long uid);

}
