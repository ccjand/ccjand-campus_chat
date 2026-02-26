package com.ccj.campus.chat.service.impl;

import com.ccj.campus.chat.dto.LeaveApplicationReq;
import com.ccj.campus.chat.dto.LeaveSubmitReq;
import com.ccj.campus.chat.service.LeaveRequestService;
import com.ccj.campus.chat.service.LeaveService;
import com.ccj.campus.chat.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @Author ccj
 * @Date 2026-01-16 17:05
 * @Description 兼容旧接口的实现，底层调用新LeaveService
 */
@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private FileService fileService;

    @Override
    public void requestLeaveApplication(LeaveApplicationReq req) {
        // 1. 转换请求参数
        LeaveSubmitReq newReq = new LeaveSubmitReq();
        newReq.setLeaveType(req.getLeaveType());
        
        // LocalDateTime -> Timestamp
        if (req.getStartTime() != null) {
            newReq.setStartTime(req.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        if (req.getEndTime() != null) {
            newReq.setEndTime(req.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        
        // CourseId -> ClassName (Temporary conversion, or just toString)
        newReq.setClassName(req.getCourseId() != null ? req.getCourseId().toString() : "");
        newReq.setReason(req.getReason());

        // 2. 处理附件 (如果有文件上传)
        if (req.getFile() != null && !req.getFile().isEmpty()) {
            try {
                // 上传文件
                var uploadResp = fileService.uploadFile(req.getUserId(), req.getFile());
                
                // 构建附件列表
                LeaveSubmitReq.AttachmentItem item = new LeaveSubmitReq.AttachmentItem();
                item.setFileType(req.getAttachmentType() != null ? req.getAttachmentType() : 1);
                item.setFileUrl(uploadResp.getFileUrl());
                item.setFileName(req.getFile().getOriginalFilename());
                item.setFileSize(req.getFile().getSize());
                
                newReq.setAttachments(Collections.singletonList(item));
            } catch (Exception e) {
                throw new RuntimeException("附件上传失败: " + e.getMessage());
            }
        } else {
            newReq.setAttachments(new ArrayList<>());
        }

        // 3. 调用新服务逻辑
        leaveService.submitLeave(req.getUserId(), newReq);
    }
}
