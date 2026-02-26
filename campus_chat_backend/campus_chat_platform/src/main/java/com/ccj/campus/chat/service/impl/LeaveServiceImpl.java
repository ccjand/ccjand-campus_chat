package com.ccj.campus.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccj.campus.chat.cache.UserInfoCache;
import com.ccj.campus.chat.dto.LeaveHistoryItemResp;
import com.ccj.campus.chat.dto.LeaveSubmitReq;
import com.ccj.campus.chat.entity.Classes;
import com.ccj.campus.chat.entity.CounselorClass;
import com.ccj.campus.chat.entity.DepartmentCounselors;
import com.ccj.campus.chat.entity.LeaveRequest;
import com.ccj.campus.chat.entity.UserClassRel;
import com.ccj.campus.chat.entity.Users;
import com.ccj.campus.chat.enums.DepartmentType;
import com.ccj.campus.chat.mapper.CounselorClassMapper;
import com.ccj.campus.chat.mapper.ClassesMapper;
import com.ccj.campus.chat.mapper.DepartmentCounselorsMapper;
import com.ccj.campus.chat.mapper.LeaveRequestMapper;
import com.ccj.campus.chat.mapper.UserClassRelMapper;
import com.ccj.campus.chat.service.LeaveService;
import com.ccj.campus.chat.utils.AssertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestMapper leaveRequestMapper;
    private final DepartmentCounselorsMapper counselorsMapper;
    private final CounselorClassMapper counselorClassMapper;
    private final ClassesMapper classesMapper;
    private final UserClassRelMapper userClassRelMapper;
    private final UserInfoCache userInfoCache;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitLeave(Long uid, LeaveSubmitReq req) {
        LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochMilli(req.getStartTime()), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochMilli(req.getEndTime()), ZoneId.systemDefault());
        
        long hours = Duration.between(start, end).toHours();
        if (hours <= 0) hours = 1;
        int days = (int) Math.ceil((double) hours / 24);
        if (days < 1) days = 1;

        Users user = userInfoCache.get(uid);
        Long approverId = resolveApproverId(uid, user, days);

        String courseId = null;
        if (user != null && user.getRole() != null && user.getRole() == 1) {
            Long classId = null;
            UserClassRel rel = userClassRelMapper.selectOne(new LambdaQueryWrapper<UserClassRel>().eq(UserClassRel::getUid, uid));
            if (rel != null) {
                classId = rel.getClassId();
            }
            if (classId == null && user != null) {
                classId = user.getClassId();
            }
            if (classId != null) {
                Classes cls = classesMapper.selectById(classId);
                if (cls != null) {
                    courseId = cls.getName();
                }
            }
        } else if (user != null && user.getDepartmentId() != null) {
            courseId = DepartmentType.getDepartmentName(user.getDepartmentId());
        }
        if (courseId == null) {
            courseId = req.getClassName();
        }
        if (courseId == null) {
            courseId = "";
        }

        Integer attachmentType = null;
        String attachmentUrl = null;
        if (req.getAttachments() != null && !req.getAttachments().isEmpty()) {
            LeaveSubmitReq.AttachmentItem item = req.getAttachments().get(0);
            attachmentType = item.getFileType();
            attachmentUrl = item.getFileUrl();
        }

        LocalDateTime now = LocalDateTime.now();

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setUserId(uid);
        leaveRequest.setLeaveType(req.getLeaveType());
        leaveRequest.setStartTime(start);
        leaveRequest.setEndTime(end);
        leaveRequest.setDurationDays(days);
        leaveRequest.setCourseId(courseId);
        leaveRequest.setReason(req.getReason());
        leaveRequest.setStatus(0);
        leaveRequest.setApproverId(approverId);
        leaveRequest.setApprovalTime(null);
        leaveRequest.setAttachmentType(attachmentType);
        leaveRequest.setAttachmentUrl(attachmentUrl);
        leaveRequest.setCreateTime(now);
        leaveRequest.setUpdateTime(now);
        leaveRequestMapper.insert(leaveRequest);
    }

    @Override
    public List<LeaveHistoryItemResp> getLeaveHistory(Long uid) {
        List<LeaveRequest> list = leaveRequestMapper.selectList(new LambdaQueryWrapper<LeaveRequest>()
                .eq(LeaveRequest::getUserId, uid)
                .orderByDesc(LeaveRequest::getCreateTime)
                .orderByDesc(LeaveRequest::getId));
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream().map(this::toHistoryResp).collect(Collectors.toList());
    }

    private LeaveHistoryItemResp toHistoryResp(LeaveRequest req) {
        LeaveHistoryItemResp resp = new LeaveHistoryItemResp();
        resp.setId(req.getId());
        resp.setLeaveType(req.getLeaveType());
        resp.setStartTime(toMillis(req.getStartTime()));
        resp.setEndTime(toMillis(req.getEndTime()));
        resp.setDurationDays(req.getDurationDays());
        resp.setCourseName(req.getCourseId());
        resp.setReason(req.getReason());
        resp.setStatus(req.getStatus());
        resp.setApproverId(req.getApproverId());
        resp.setApproverComment(req.getApproverComment());
        resp.setApprovalTime(toMillis(req.getApprovalTime()));
        resp.setAttachmentType(req.getAttachmentType());
        resp.setAttachmentUrl(req.getAttachmentUrl());
        resp.setCreateTime(toMillis(req.getCreateTime()));
        resp.setUpdateTime(toMillis(req.getUpdateTime()));
        return resp;
    }

    private long toMillis(LocalDateTime time) {
        if (time == null) {
            return 0L;
        }
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private Long resolveApproverId(Long uid, Users user, int durationDays) {
        if (user == null) {
            return null;
        }
        if (user.getRole() != null && user.getRole() == 1) {
            Long classId = getStudentClassId(uid, user);
            AssertUtil.isNotEmpty(classId, "未找到学生所属班级");
            if (durationDays <= 2) {
                Long headTeacherUid = getHeadTeacherUid(classId);
                AssertUtil.isNotEmpty(headTeacherUid, "未配置班主任");
                return headTeacherUid;
            }
            Long counselorUid = getCounselorUidByClassId(classId);
            AssertUtil.isNotEmpty(counselorUid, "未配置辅导员");
            return counselorUid;
        }
        return getCounselorUid(user.getDepartmentId());
    }

    private Long getStudentClassId(Long uid, Users user) {
        UserClassRel rel = userClassRelMapper.selectOne(new LambdaQueryWrapper<UserClassRel>().eq(UserClassRel::getUid, uid));
        if (rel != null && rel.getClassId() != null) {
            return rel.getClassId();
        }
        return user.getClassId();
    }

    private Long getHeadTeacherUid(Long classId) {
        if (classId == null) {
            return null;
        }
        Classes cls = classesMapper.selectById(classId);
        if (cls == null) {
            return null;
        }
        return cls.getHeadTeacherUid();
    }

    private Long getCounselorUidByClassId(Long classId) {
        if (classId == null) {
            return null;
        }
        CounselorClass rel = counselorClassMapper.selectOne(new LambdaQueryWrapper<CounselorClass>()
                .eq(CounselorClass::getClassId, classId)
                .orderByAsc(CounselorClass::getId)
                .last("LIMIT 1"));
        if (rel != null && rel.getCounselorUid() != null) {
            return rel.getCounselorUid();
        }
        Long departmentId = getDepartmentIdByClassId(classId);
        return getCounselorUid(departmentId);
    }

    private Long getDepartmentIdByClassId(Long classId) {
        if (classId == null) {
            return null;
        }
        Classes cls = classesMapper.selectById(classId);
        if (cls == null) {
            return null;
        }
        return cls.getDepartmentId();
    }

    private Long getCounselorUid(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        List<DepartmentCounselors> counselors = counselorsMapper.selectList(new LambdaQueryWrapper<DepartmentCounselors>()
                .eq(DepartmentCounselors::getDepartmentId, departmentId)
                .orderByAsc(DepartmentCounselors::getId));
        if (counselors == null || counselors.isEmpty()) {
            return null;
        }
        return counselors.get(0).getCounselorUid();
    }
}
