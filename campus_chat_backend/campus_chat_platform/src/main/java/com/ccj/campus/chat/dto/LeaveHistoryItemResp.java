package com.ccj.campus.chat.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LeaveHistoryItemResp implements Serializable {
    private Long id;
    private Integer leaveType;
    private Long startTime;
    private Long endTime;
    private Integer durationDays;
    private String courseName;
    private String reason;
    private Integer status;
    private Long approverId;
    private String approverComment;
    private Long approvalTime;
    private Integer attachmentType;
    private String attachmentUrl;
    private Long createTime;
    private Long updateTime;
}
