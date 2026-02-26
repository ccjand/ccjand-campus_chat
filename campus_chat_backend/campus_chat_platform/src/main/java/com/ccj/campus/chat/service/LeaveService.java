package com.ccj.campus.chat.service;

import com.ccj.campus.chat.dto.LeaveHistoryItemResp;
import com.ccj.campus.chat.dto.LeaveSubmitReq;

import java.util.List;

public interface LeaveService {
    void submitLeave(Long uid, LeaveSubmitReq req);

    List<LeaveHistoryItemResp> getLeaveHistory(Long uid);
}
