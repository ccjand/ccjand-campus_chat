package com.ccj.campus.chat.service;

import com.ccj.campus.chat.dto.*;

import java.util.List;

public interface CheckInService {
    List<CheckInTeacherCourseResp> getTeacherCourses(Long teacherUid);

    List<CheckInTeacherClassResp> getTeacherCourseClasses(Long teacherUid, Long courseId);

    CreateCheckInSessionResp createSession(Long teacherUid, CreateCheckInSessionReq req);

    CheckInTeacherSessionStatsResp getTeacherSessionStats(Long teacherUid, Long sessionId);

    List<CheckInStudentActiveSessionResp> getStudentActiveSessions(Long studentUid);

    List<CheckInStudentHistoryCourseResp> getStudentHistory(Long studentUid);

    StudentCheckInResp studentCheckIn(Long studentUid, StudentCheckInReq req);

    TeacherCheckInCodeResp generateSessionCode(Long teacherUid, Long sessionId, String desiredCode);

    TeacherCheckInQrCodeResp generateSessionQrCode(Long teacherUid, Long sessionId);

    StudentCheckInResp studentCheckInByCode(Long studentUid, StudentCheckInCodeReq req);

    StudentCheckInResp studentCheckInByQrCode(Long studentUid, StudentCheckInQrCodeReq req);
}
