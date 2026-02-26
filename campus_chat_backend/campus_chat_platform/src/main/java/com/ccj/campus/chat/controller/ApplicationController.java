package com.ccj.campus.chat.controller;

import com.ccj.campus.chat.dto.ApiResult;
import com.ccj.campus.chat.dto.LeaveApplicationReq;
import com.ccj.campus.chat.dto.LeaveApplicationResp;
import com.ccj.campus.chat.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @Author ccj
 * @Date 2026-01-16 16:41
 * @Description
 */
@RestController
@RequestMapping("/application")
public class ApplicationController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    /**
     * 请假申请
     */
    @PostMapping(value = "/leave", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResult<String> requestLeaveApplication(@RequestBody @Valid LeaveApplicationReq req) {
        leaveRequestService.requestLeaveApplication(req);
        return ApiResult.success("申请成功");
    }

}
