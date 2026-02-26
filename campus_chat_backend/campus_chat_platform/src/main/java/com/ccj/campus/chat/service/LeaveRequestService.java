package com.ccj.campus.chat.service;

import com.ccj.campus.chat.dto.LeaveApplicationReq;

/**
 * @Author ccj
 * @Date 2026-01-16 17:04
 * @Description
 */
public interface LeaveRequestService {
    /**
     * 请假申请
     */
    void requestLeaveApplication(LeaveApplicationReq req);
}
