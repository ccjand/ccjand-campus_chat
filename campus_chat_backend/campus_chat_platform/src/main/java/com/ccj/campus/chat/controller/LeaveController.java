package com.ccj.campus.chat.controller;

import com.ccj.campus.chat.dto.ApiResult;
import com.ccj.campus.chat.dto.LeaveHistoryItemResp;
import com.ccj.campus.chat.dto.LeaveSubmitReq;
import com.ccj.campus.chat.service.LeaveService;
import com.ccj.campus.chat.util.RequestHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/leave")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @PostMapping("/submit")
    public ApiResult<Void> submitLeave(@RequestBody @Valid LeaveSubmitReq req) {
        Long uid = RequestHolder.get().getUid();
        leaveService.submitLeave(uid, req);
        return ApiResult.success();
    }

    @GetMapping("/history")
    public ApiResult<List<LeaveHistoryItemResp>> getLeaveHistory() {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(leaveService.getLeaveHistory(uid));
    }
}
