package com.ccj.campus.chat.controller;

import com.ccj.campus.chat.dto.*;
import com.ccj.campus.chat.service.CheckInService;
import com.ccj.campus.chat.util.RequestHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/checkin")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @GetMapping("/teacher/courses")
    public ApiResult<List<CheckInTeacherCourseResp>> teacherCourses() {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(checkInService.getTeacherCourses(uid));
    }

    @GetMapping("/teacher/course/{courseId}/classes")
    public ApiResult<List<CheckInTeacherClassResp>> teacherCourseClasses(@PathVariable("courseId") Long courseId) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(checkInService.getTeacherCourseClasses(uid, courseId));
    }

    @PostMapping("/teacher/session")
    public ApiResult<CreateCheckInSessionResp> createSession(@RequestBody @Valid CreateCheckInSessionReq req) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(checkInService.createSession(uid, req));
    }

    @GetMapping("/teacher/session/{sessionId}/stats")
    public ApiResult<CheckInTeacherSessionStatsResp> teacherSessionStats(@PathVariable("sessionId") Long sessionId) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(checkInService.getTeacherSessionStats(uid, sessionId));
    }

    @PostMapping("/teacher/session/{sessionId}/code")
    public ApiResult<TeacherCheckInCodeResp> teacherSessionCode(@PathVariable("sessionId") Long sessionId,
                                                                @RequestBody(required = false) Map<String, Object> req) {
        Long uid = RequestHolder.get().getUid();
        String desiredCode = null;
        if (req != null) {
            Object code = req.get("code");
            if (code != null) {
                desiredCode = String.valueOf(code);
            }
        }
        return ApiResult.success(checkInService.generateSessionCode(uid, sessionId, desiredCode));
    }

    @PostMapping("/teacher/session/{sessionId}/qrcode")
    public ApiResult<TeacherCheckInQrCodeResp> teacherSessionQrCode(@PathVariable("sessionId") Long sessionId) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(checkInService.generateSessionQrCode(uid, sessionId));
    }

    @GetMapping("/student/active")
    public ApiResult<List<CheckInStudentActiveSessionResp>> studentActiveSessions() {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(checkInService.getStudentActiveSessions(uid));
    }

    @GetMapping("/student/history")
    public ApiResult<List<CheckInStudentHistoryCourseResp>> studentHistory() {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(checkInService.getStudentHistory(uid));
    }

    @PostMapping("/student/checkin")
    public ApiResult<StudentCheckInResp> studentCheckIn(@RequestBody @Valid StudentCheckInReq req) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(checkInService.studentCheckIn(uid, req));
    }

    @PostMapping("/student/checkin/code")
    public ApiResult<StudentCheckInResp> studentCheckInByCode(@RequestBody @Valid StudentCheckInCodeReq req) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(checkInService.studentCheckInByCode(uid, req));
    }

    @PostMapping("/student/checkin/qrcode")
    public ApiResult<StudentCheckInResp> studentCheckInByQrCode(@RequestBody @Valid StudentCheckInQrCodeReq req) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(checkInService.studentCheckInByQrCode(uid, req));
    }
}
