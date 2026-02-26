package com.ccj.campus.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccj.campus.chat.cache.UserInfoCache;
import com.ccj.campus.chat.dto.*;
import com.ccj.campus.chat.entity.*;
import com.ccj.campus.chat.mapper.*;
import com.ccj.campus.chat.service.CheckInService;
import com.ccj.campus.chat.utils.AssertUtil;
import com.ccj.campus.chat.utils.RedisKey;
import com.ccj.campus.chat.utils.RedisUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    private final TeacherCourseMapper teacherCourseMapper;
    private final CoursesMapper coursesMapper;
    private final CourseClassMapper courseClassMapper;
    private final ClassesMapper classesMapper;
    private final CheckInSessionMapper checkInSessionMapper;
    private final CheckInSessionClassMapper checkInSessionClassMapper;
    private final CheckInsMapper checkInsMapper;
    private final UserClassRelMapper userClassRelMapper;
    private final UserInfoCache userInfoCache;
    private final CheckInQrCodeMapper checkInQrCodeMapper;

    private static final String CHECKIN_QR_PREFIX = "ccj-checkin:";

    @Override
    public List<CheckInTeacherCourseResp> getTeacherCourses(Long teacherUid) {
        assertTeacher(teacherUid);

        List<TeacherCourse> relList = teacherCourseMapper.selectList(new LambdaQueryWrapper<TeacherCourse>()
                .eq(TeacherCourse::getTeacherUid, teacherUid)
                .orderByAsc(TeacherCourse::getId));
        if (relList == null || relList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> courseIds = relList.stream()
                .map(TeacherCourse::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Courses> courses = coursesMapper.selectList(new LambdaQueryWrapper<Courses>()
                .in(Courses::getId, courseIds)
                .orderByAsc(Courses::getId));

        if (courses == null || courses.isEmpty()) {
            return Collections.emptyList();
        }

        return courses.stream().map(c -> {
            CheckInTeacherCourseResp resp = new CheckInTeacherCourseResp();
            resp.setCourseId(c.getId());
            resp.setCourseName(c.getName());
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CheckInTeacherClassResp> getTeacherCourseClasses(Long teacherUid, Long courseId) {
        assertTeacher(teacherUid);
        AssertUtil.isNotEmpty(courseId, "courseId不能为空");
        assertTeacherOwnsCourse(teacherUid, courseId);

        List<CourseClass> relList = courseClassMapper.selectList(new LambdaQueryWrapper<CourseClass>()
                .eq(CourseClass::getCourseId, courseId)
                .orderByAsc(CourseClass::getId));
        if (relList == null || relList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> classIds = relList.stream()
                .map(CourseClass::getClassId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (classIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Classes> classes = classesMapper.selectList(new LambdaQueryWrapper<Classes>()
                .in(Classes::getId, classIds)
                .orderByAsc(Classes::getId));
        if (classes == null || classes.isEmpty()) {
            return Collections.emptyList();
        }

        return classes.stream().map(cls -> {
            CheckInTeacherClassResp resp = new CheckInTeacherClassResp();
            resp.setClassId(cls.getId());
            resp.setClassName(cls.getName());
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateCheckInSessionResp createSession(Long teacherUid, CreateCheckInSessionReq req) {
        assertTeacher(teacherUid);
        AssertUtil.isNotEmpty(req, "请求不能为空");

        Long courseId = req.getCourseId();
        AssertUtil.isNotEmpty(courseId, "课程不能为空");
        assertTeacherOwnsCourse(teacherUid, courseId);

        Integer radiusMeters = req.getRadiusMeters();
        Integer durationMinutes = req.getDurationMinutes();
        AssertUtil.isNotEmpty(radiusMeters, "签到半径不能为空");
        AssertUtil.isNotEmpty(durationMinutes, "有效时长不能为空");
        AssertUtil.isTrue(radiusMeters > 0, "签到半径必须大于0");
        AssertUtil.isTrue(durationMinutes > 0, "有效时长必须大于0");
        AssertUtil.isNotEmpty(req.getCenterLatitude(), "中心纬度不能为空");
        AssertUtil.isNotEmpty(req.getCenterLongitude(), "中心经度不能为空");

        List<Long> classIds = Optional.ofNullable(req.getClassIds()).orElse(Collections.emptyList()).stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        AssertUtil.isTrue(!classIds.isEmpty(), "班级不能为空");
        assertCourseContainsClasses(courseId, classIds);

        LocalDateTime now = LocalDateTime.now();
        CheckInSession session = new CheckInSession();
        session.setCourseId(courseId);
        session.setTeacherUid(teacherUid);
        session.setTitle(req.getTitle());
        session.setCenterLatitude(req.getCenterLatitude());
        session.setCenterLongitude(req.getCenterLongitude());
        session.setRadiusMeters(radiusMeters);
        session.setDurationMinutes(durationMinutes);
        session.setStatus(1);
        session.setStartTime(now);
        session.setCreatedTime(now);
        session.setUpdatedTime(now);
        checkInSessionMapper.insert(session);

        AssertUtil.isNotEmpty(session.getId(), "创建失败");

        for (Long classId : classIds) {
            CheckInSessionClass rel = new CheckInSessionClass();
            rel.setSessionId(session.getId());
            rel.setClassId(classId);
            rel.setCreatedTime(now);
            checkInSessionClassMapper.insert(rel);
        }

        CheckInSession persisted = checkInSessionMapper.selectById(session.getId());
        CreateCheckInSessionResp resp = new CreateCheckInSessionResp();
        resp.setSessionId(session.getId());
        LocalDateTime persistedStartTime = persisted != null ? persisted.getStartTime() : null;
        if (persistedStartTime == null) {
            persistedStartTime = session.getStartTime();
        }
        resp.setStartTime(toMillis(persistedStartTime));
        LocalDateTime persistedEndTime = persisted != null ? persisted.getEndTime() : null;
        if (persistedEndTime == null) {
            if (persistedStartTime != null && session.getDurationMinutes() != null) {
                persistedEndTime = persistedStartTime.plusMinutes(session.getDurationMinutes().longValue());
            }
        }
        resp.setEndTime(toMillis(persistedEndTime));
        return resp;
    }

    @Override
    public CheckInTeacherSessionStatsResp getTeacherSessionStats(Long teacherUid, Long sessionId) {
        assertTeacher(teacherUid);
        AssertUtil.isNotEmpty(sessionId, "sessionId不能为空");

        CheckInSession session = checkInSessionMapper.selectById(sessionId);
        AssertUtil.isNotEmpty(session, "签到不存在");
        AssertUtil.equal(session.getTeacherUid(), teacherUid, "无权限查看该签到");

        List<CheckInSessionClass> relList = checkInSessionClassMapper.selectList(new LambdaQueryWrapper<CheckInSessionClass>()
                .eq(CheckInSessionClass::getSessionId, sessionId)
                .orderByAsc(CheckInSessionClass::getId));
        List<Long> classIds = relList == null ? Collections.emptyList() : relList.stream()
                .map(CheckInSessionClass::getClassId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> classNameMap = new HashMap<>();
        if (!classIds.isEmpty()) {
            List<Classes> classes = classesMapper.selectList(new LambdaQueryWrapper<Classes>().in(Classes::getId, classIds));
            if (classes != null) {
                classNameMap = classes.stream().collect(Collectors.toMap(Classes::getId, Classes::getName, (a, b) -> a));
            }
        }

        List<CheckInTeacherSessionStatsResp.ClassStats> statsList = new ArrayList<>();
        int total = 0;
        int checked = 0;
        for (Long classId : classIds) {
            int totalClass = Math.toIntExact(userClassRelMapper.selectCount(new LambdaQueryWrapper<UserClassRel>()
                    .eq(UserClassRel::getClassId, classId)));
            int checkedClass = Math.toIntExact(checkInsMapper.selectCount(new LambdaQueryWrapper<CheckIns>()
                    .eq(CheckIns::getSessionId, sessionId)
                    .eq(CheckIns::getClassId, classId)));
            total += totalClass;
            checked += checkedClass;
            CheckInTeacherSessionStatsResp.ClassStats cs = new CheckInTeacherSessionStatsResp.ClassStats();
            cs.setClassId(classId);
            cs.setClassName(classNameMap.getOrDefault(classId, ""));
            cs.setTotalStudents(totalClass);
            cs.setCheckedInStudents(checkedClass);
            statsList.add(cs);
        }

        CheckInTeacherSessionStatsResp resp = new CheckInTeacherSessionStatsResp();
        resp.setSessionId(sessionId);
        resp.setTotalStudents(total);
        resp.setCheckedInStudents(checked);
        resp.setClassStats(statsList);
        return resp;
    }

    @Override
    public List<CheckInStudentActiveSessionResp> getStudentActiveSessions(Long studentUid) {
        assertStudent(studentUid);

        Long classId = getStudentClassId(studentUid);
        AssertUtil.isNotEmpty(classId, "未找到学生所属班级");

        List<CheckInSessionClass> relList = checkInSessionClassMapper.selectList(new LambdaQueryWrapper<CheckInSessionClass>()
                .eq(CheckInSessionClass::getClassId, classId)
                .orderByDesc(CheckInSessionClass::getId));
        if (relList == null || relList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> sessionIds = relList.stream()
                .map(CheckInSessionClass::getSessionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (sessionIds.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime now = LocalDateTime.now();
        List<CheckInSession> sessions = checkInSessionMapper.selectList(new LambdaQueryWrapper<CheckInSession>()
                .in(CheckInSession::getId, sessionIds)
                .eq(CheckInSession::getStatus, 1)
                .le(CheckInSession::getStartTime, now)
                .orderByDesc(CheckInSession::getStartTime));

        if (sessions != null && !sessions.isEmpty()) {
            sessions = sessions.stream()
                    .filter(s -> {
                        LocalDateTime effectiveEndTime = s.getEndTime();
                        if (effectiveEndTime == null && s.getStartTime() != null && s.getDurationMinutes() != null) {
                            effectiveEndTime = s.getStartTime().plusMinutes(s.getDurationMinutes().longValue());
                        }
                        return effectiveEndTime != null && !effectiveEndTime.isBefore(now);
                    })
                    .collect(Collectors.toList());
        }

        if (sessions == null || sessions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> currentSessionIds = sessions.stream()
                .map(CheckInSession::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (currentSessionIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> checkedSessionIds = Optional.ofNullable(checkInsMapper.selectList(new LambdaQueryWrapper<CheckIns>()
                        .select(CheckIns::getSessionId)
                        .eq(CheckIns::getUserId, studentUid)
                        .in(CheckIns::getSessionId, currentSessionIds)))
                .orElse(Collections.emptyList())
                .stream()
                .map(CheckIns::getSessionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<CheckInSession> uncheckedSessions = sessions.stream()
                .filter(s -> s.getId() != null && !checkedSessionIds.contains(s.getId()))
                .collect(Collectors.toList());

        if (uncheckedSessions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> courseIds = uncheckedSessions.stream()
                .map(CheckInSession::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        final Map<Long, String> courseNameMap;
        if (courseIds.isEmpty()) {
            courseNameMap = Collections.emptyMap();
        } else {
            List<Courses> courses = coursesMapper.selectList(new LambdaQueryWrapper<Courses>().in(Courses::getId, courseIds));
            if (courses == null || courses.isEmpty()) {
                courseNameMap = Collections.emptyMap();
            } else {
                courseNameMap = courses.stream().collect(Collectors.toMap(Courses::getId, Courses::getName, (a, b) -> a));
            }
        }

        return uncheckedSessions.stream().map(s -> {
            LocalDateTime effectiveEndTime = resolveEffectiveEndTime(s);
            CheckInStudentActiveSessionResp resp = new CheckInStudentActiveSessionResp();
            resp.setSessionId(s.getId());
            resp.setCourseId(s.getCourseId());
            resp.setCourseName(courseNameMap.getOrDefault(s.getCourseId(), ""));
            resp.setTitle(s.getTitle());
            resp.setRadiusMeters(s.getRadiusMeters());
            resp.setCenterLatitude(s.getCenterLatitude());
            resp.setCenterLongitude(s.getCenterLongitude());
            resp.setStartTime(toMillis(s.getStartTime()));
            resp.setEndTime(toMillis(effectiveEndTime));
            resp.setCheckedIn(false);
            String sessionCode = RedisUtils.getStr(RedisKey.getKey(RedisKey.CHECKIN_SESSION_CODE, s.getId()));
            resp.setCodeEnabled(sessionCode != null && !sessionCode.isBlank());
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CheckInStudentHistoryCourseResp> getStudentHistory(Long studentUid) {
        assertStudent(studentUid);

        Long classId = getStudentClassId(studentUid);
        AssertUtil.isNotEmpty(classId, "未找到学生所属班级");

        List<CheckInSessionClass> relList = checkInSessionClassMapper.selectList(new LambdaQueryWrapper<CheckInSessionClass>()
                .eq(CheckInSessionClass::getClassId, classId)
                .orderByDesc(CheckInSessionClass::getId));
        if (relList == null || relList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> sessionIds = relList.stream()
                .map(CheckInSessionClass::getSessionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (sessionIds.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime now = LocalDateTime.now();
        List<CheckInSession> sessions = checkInSessionMapper.selectList(new LambdaQueryWrapper<CheckInSession>()
                .in(CheckInSession::getId, sessionIds)
                .le(CheckInSession::getStartTime, now)
                .orderByDesc(CheckInSession::getStartTime));
        if (sessions == null || sessions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> courseIds = sessions.stream()
                .map(CheckInSession::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        final Map<Long, String> courseNameMap;
        if (courseIds.isEmpty()) {
            courseNameMap = Collections.emptyMap();
        } else {
            List<Courses> courses = coursesMapper.selectList(new LambdaQueryWrapper<Courses>().in(Courses::getId, courseIds));
            if (courses == null || courses.isEmpty()) {
                courseNameMap = Collections.emptyMap();
            } else {
                courseNameMap = courses.stream().collect(Collectors.toMap(Courses::getId, Courses::getName, (a, b) -> a));
            }
        }

        List<Long> actualSessionIds = sessions.stream()
                .map(CheckInSession::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        final Map<Long, CheckIns> checkedMap;
        if (actualSessionIds.isEmpty()) {
            checkedMap = Collections.emptyMap();
        } else {
            List<CheckIns> checkedList = checkInsMapper.selectList(new LambdaQueryWrapper<CheckIns>()
                    .eq(CheckIns::getUserId, studentUid)
                    .in(CheckIns::getSessionId, actualSessionIds));
            if (checkedList == null || checkedList.isEmpty()) {
                checkedMap = Collections.emptyMap();
            } else {
                checkedMap = checkedList.stream()
                        .filter(r -> r.getSessionId() != null)
                        .collect(Collectors.toMap(CheckIns::getSessionId, r -> r, (a, b) -> a));
            }
        }

        LinkedHashMap<Long, List<CheckInSession>> courseToSessions = new LinkedHashMap<>();
        for (CheckInSession s : sessions) {
            if (s.getCourseId() == null) {
                continue;
            }
            courseToSessions.computeIfAbsent(s.getCourseId(), k -> new ArrayList<>()).add(s);
        }

        List<CheckInStudentHistoryCourseResp> respList = new ArrayList<>();
        for (Map.Entry<Long, List<CheckInSession>> entry : courseToSessions.entrySet()) {
            Long courseId = entry.getKey();
            CheckInStudentHistoryCourseResp courseResp = new CheckInStudentHistoryCourseResp();
            courseResp.setCourseId(courseId);
            courseResp.setCourseName(courseNameMap.getOrDefault(courseId, ""));

            List<CheckInStudentHistoryCourseResp.Record> itemList = new ArrayList<>();
            for (CheckInSession s : entry.getValue()) {
                CheckIns checked = s.getId() == null ? null : checkedMap.get(s.getId());
                LocalDateTime effectiveEndTime = resolveEffectiveEndTime(s);
                CheckInStudentHistoryCourseResp.Record item = new CheckInStudentHistoryCourseResp.Record();
                item.setCheckInId(checked == null ? null : checked.getId());
                item.setSessionId(s.getId());
                item.setSessionTitle(s.getTitle());
                item.setStartTime(toMillis(s.getStartTime()));
                item.setEndTime(toMillis(effectiveEndTime));
                item.setCheckedIn(checked != null);
                item.setCheckInTime(toMillis(checked == null ? null : checked.getCheckInTime()));
                itemList.add(item);
            }
            courseResp.setRecords(itemList);
            respList.add(courseResp);
        }
        return respList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentCheckInResp studentCheckIn(Long studentUid, StudentCheckInReq req) {
        assertStudent(studentUid);
        AssertUtil.isNotEmpty(req, "请求不能为空");
        AssertUtil.isNotEmpty(req.getSessionId(), "sessionId不能为空");
        AssertUtil.isNotEmpty(req.getLatitude(), "纬度不能为空");
        AssertUtil.isNotEmpty(req.getLongitude(), "经度不能为空");

        Long classId = getStudentClassId(studentUid);
        AssertUtil.isNotEmpty(classId, "未找到学生所属班级");

        CheckInSession session = checkInSessionMapper.selectById(req.getSessionId());
        AssertUtil.isNotEmpty(session, "签到不存在");
        AssertUtil.isTrue(Objects.equals(session.getStatus(), 1), "签到已结束");

        LocalDateTime now = LocalDateTime.now();
        AssertUtil.isTrue(session.getStartTime() != null, "签到时间配置异常");
        LocalDateTime effectiveEndTime = session.getEndTime();
        if (effectiveEndTime == null && session.getDurationMinutes() != null) {
            effectiveEndTime = session.getStartTime().plusMinutes(session.getDurationMinutes().longValue());
        }
        AssertUtil.isTrue(effectiveEndTime != null, "签到时间配置异常");
        AssertUtil.isTrue(!now.isBefore(session.getStartTime()) && !now.isAfter(effectiveEndTime), "不在签到有效时间内");

        long relCount = checkInSessionClassMapper.selectCount(new LambdaQueryWrapper<CheckInSessionClass>()
                .eq(CheckInSessionClass::getSessionId, session.getId())
                .eq(CheckInSessionClass::getClassId, classId));
        AssertUtil.isTrue(relCount > 0, "不在本次签到班级范围内");

        long existCount = checkInsMapper.selectCount(new LambdaQueryWrapper<CheckIns>()
                .eq(CheckIns::getUserId, studentUid)
                .eq(CheckIns::getSessionId, session.getId()));
        AssertUtil.isTrue(existCount == 0, "已签到");

        double distanceMeters = haversineMeters(
                session.getCenterLatitude(),
                session.getCenterLongitude(),
                req.getLatitude(),
                req.getLongitude()
        );

        double toleranceMeters = 0.0;
        if (req.getAccuracy() != null) {
            double a = req.getAccuracy().doubleValue();
            if (Double.isFinite(a) && a > 0) {
                toleranceMeters = Math.min(a, 50.0);
            }
        }
        double allowedMeters = (double) session.getRadiusMeters() + toleranceMeters;
        AssertUtil.isTrue(distanceMeters <= allowedMeters,
                String.format("不在签到范围内(距离%.2fm, 允许%.2fm)", distanceMeters, allowedMeters));

        CheckIns record = new CheckIns();
        record.setUserId(studentUid);
        record.setCourseId(session.getCourseId());
        record.setSessionId(session.getId());
        record.setClassId(classId);
        record.setCheckInTime(now);
        record.setLocation(req.getLocation());
        record.setLatitude(req.getLatitude());
        record.setLongitude(req.getLongitude());
        record.setAccuracy(req.getAccuracy());
        record.setStatus(1);
        record.setCreateTime(now);
        record.setUpdateTime(now);
        checkInsMapper.insert(record);

        StudentCheckInResp resp = new StudentCheckInResp();
        resp.setCheckInId(record.getId());
        resp.setSessionId(session.getId());
        resp.setDistanceMeters(round(distanceMeters, 2));
        resp.setCheckInTime(toMillis(now));
        return resp;
    }

    @Override
    public TeacherCheckInCodeResp generateSessionCode(Long teacherUid, Long sessionId, String desiredCode) {
        assertTeacher(teacherUid);
        AssertUtil.isNotEmpty(sessionId, "sessionId不能为空");

        CheckInSession session = checkInSessionMapper.selectById(sessionId);
        AssertUtil.isNotEmpty(session, "签到不存在");
        AssertUtil.equal(session.getTeacherUid(), teacherUid, "无权限操作该签到");
        AssertUtil.isTrue(Objects.equals(session.getStatus(), 1), "签到已结束");

        LocalDateTime now = LocalDateTime.now();
        AssertUtil.isTrue(session.getStartTime() != null, "签到时间配置异常");
        LocalDateTime effectiveEndTime = resolveEffectiveEndTime(session);
        AssertUtil.isTrue(effectiveEndTime != null, "签到时间配置异常");
        AssertUtil.isTrue(!now.isAfter(effectiveEndTime), "签到已结束");

        long ttlSeconds = Duration.between(now, effectiveEndTime).getSeconds();
        if (ttlSeconds <= 0) {
            ttlSeconds = 1;
        }

        String sessionKey = RedisKey.getKey(RedisKey.CHECKIN_SESSION_CODE, sessionId);
        String oldCode = RedisUtils.getStr(sessionKey);
        String desired = desiredCode == null ? null : desiredCode.trim();
        if (desired != null && desired.isBlank()) {
            desired = null;
        }
        if (desired != null) {
            AssertUtil.isTrue(desired.matches("^\\d{4}$"), "签到码必须为4位数字");
        }

        String sessionIdStr = String.valueOf(sessionId);
        String code;
        if (desired != null) {
            code = desired;
        } else {
            code = String.format("%04d", ThreadLocalRandom.current().nextInt(0, 10000));
        }

        RedisUtils.set(sessionKey, code, ttlSeconds);
        String codeSessionsKey = RedisKey.getKey(RedisKey.CHECKIN_CODE_SESSIONS, code);
        RedisUtils.sSetOne(codeSessionsKey, sessionIdStr);
        Long codeSessionsTtl = RedisUtils.getExpire(codeSessionsKey);
        if (codeSessionsTtl != null && codeSessionsTtl == -1) {
            // keep
        } else {
            long curSeconds = codeSessionsTtl == null || codeSessionsTtl < 0 ? 0 : codeSessionsTtl;
            long nextSeconds = Math.max(curSeconds, ttlSeconds);
            RedisUtils.expire(codeSessionsKey, nextSeconds);
        }

        if (oldCode != null && !oldCode.isBlank() && !oldCode.equals(code)) {
            String oldCodeSessionsKey = RedisKey.getKey(RedisKey.CHECKIN_CODE_SESSIONS, oldCode);
            RedisUtils.setRemove(oldCodeSessionsKey, sessionIdStr);
            Long remain = RedisUtils.sGetSetSize(oldCodeSessionsKey);
            if (remain != null && remain <= 0) {
                RedisUtils.del(oldCodeSessionsKey);
            }
        }

        TeacherCheckInCodeResp resp = new TeacherCheckInCodeResp();
        resp.setSessionId(sessionId);
        resp.setCode(code);
        resp.setExpireSeconds(ttlSeconds);
        resp.setExpireAt(toMillis(now.plusSeconds(ttlSeconds)));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeacherCheckInQrCodeResp generateSessionQrCode(Long teacherUid, Long sessionId) {
        assertTeacher(teacherUid);
        AssertUtil.isNotEmpty(sessionId, "sessionId不能为空");

        CheckInSession session = checkInSessionMapper.selectById(sessionId);
        AssertUtil.isNotEmpty(session, "签到不存在");
        AssertUtil.equal(session.getTeacherUid(), teacherUid, "无权限操作该签到");
        AssertUtil.isTrue(Objects.equals(session.getStatus(), 1), "签到已结束");

        LocalDateTime now = LocalDateTime.now();
        AssertUtil.isTrue(session.getStartTime() != null, "签到时间配置异常");
        LocalDateTime effectiveEndTime = resolveEffectiveEndTime(session);
        AssertUtil.isTrue(effectiveEndTime != null, "签到时间配置异常");
        AssertUtil.isTrue(!now.isAfter(effectiveEndTime), "签到已结束");

        long remainSeconds = Duration.between(now, effectiveEndTime).getSeconds();
        if (remainSeconds <= 0) {
            remainSeconds = 1;
        }
        long ttlSeconds = Math.min(10, remainSeconds);
        LocalDateTime qrExpireAt = now.plusSeconds(ttlSeconds);

        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        String content = CHECKIN_QR_PREFIX + token;
        String tokenKey = RedisKey.getKey(RedisKey.CHECKIN_QR_TOKEN_SESSION, token);
        RedisUtils.set(tokenKey, String.valueOf(sessionId), ttlSeconds);

        String imageBase64 = buildQrPngDataUrl(content);

        TeacherCheckInQrCodeResp resp = new TeacherCheckInQrCodeResp();
        resp.setSessionId(sessionId);
        resp.setContent(content);
        resp.setImageBase64(imageBase64);
        resp.setExpireSeconds(ttlSeconds);
        resp.setExpireAt(toMillis(qrExpireAt));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentCheckInResp studentCheckInByCode(Long studentUid, StudentCheckInCodeReq req) {
        assertStudent(studentUid);
        AssertUtil.isNotEmpty(req, "请求不能为空");
        AssertUtil.isNotEmpty(req.getCode(), "签到码不能为空");

        String code = req.getCode() == null ? null : req.getCode().trim();
        AssertUtil.isNotEmpty(code, "签到码不能为空");

        String codeSessionsKey = RedisKey.getKey(RedisKey.CHECKIN_CODE_SESSIONS, code);
        Set<String> sessionIdStrSet = RedisUtils.sGet(codeSessionsKey);
        AssertUtil.isTrue(sessionIdStrSet != null && !sessionIdStrSet.isEmpty(), "签到码无效或已过期");

        List<Long> sessionIds = new ArrayList<>();
        for (String sessionIdStr : sessionIdStrSet) {
            if (sessionIdStr == null || sessionIdStr.isBlank()) {
                continue;
            }
            Long sid;
            try {
                sid = Long.parseLong(sessionIdStr.trim());
            } catch (Exception e) {
                sid = null;
            }
            if (sid == null) {
                RedisUtils.setRemove(codeSessionsKey, sessionIdStr);
                continue;
            }
            String sessionKey = RedisKey.getKey(RedisKey.CHECKIN_SESSION_CODE, sid);
            String mappedCode = RedisUtils.getStr(sessionKey);
            if (mappedCode == null || mappedCode.isBlank() || !code.equals(mappedCode.trim())) {
                RedisUtils.setRemove(codeSessionsKey, sessionIdStr);
                continue;
            }
            sessionIds.add(sid);
        }
        AssertUtil.isTrue(!sessionIds.isEmpty(), "签到码无效或已过期");

        Long classId = getStudentClassId(studentUid);
        AssertUtil.isNotEmpty(classId, "未找到学生所属班级");

        LocalDateTime now = LocalDateTime.now();
        List<CheckInSession> sessions = checkInSessionMapper.selectList(new LambdaQueryWrapper<CheckInSession>()
                .in(CheckInSession::getId, sessionIds)
                .eq(CheckInSession::getStatus, 1)
                .le(CheckInSession::getStartTime, now)
                .orderByDesc(CheckInSession::getStartTime)
                .orderByDesc(CheckInSession::getId));
        if (sessions != null && !sessions.isEmpty()) {
            sessions = sessions.stream()
                    .filter(s -> {
                        LocalDateTime end = resolveEffectiveEndTime(s);
                        return end != null && !now.isAfter(end);
                    })
                    .collect(Collectors.toList());
        }
        AssertUtil.isTrue(sessions != null && !sessions.isEmpty(), "签到码无效或已过期");

        List<Long> candidateSessionIds = sessions.stream()
                .map(CheckInSession::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Set<Long> allowedSessionIds = Optional.ofNullable(checkInSessionClassMapper.selectList(new LambdaQueryWrapper<CheckInSessionClass>()
                        .select(CheckInSessionClass::getSessionId)
                        .eq(CheckInSessionClass::getClassId, classId)
                        .in(CheckInSessionClass::getSessionId, candidateSessionIds)))
                .orElse(Collections.emptyList())
                .stream()
                .map(CheckInSessionClass::getSessionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> checkedSessionIds = Optional.ofNullable(checkInsMapper.selectList(new LambdaQueryWrapper<CheckIns>()
                        .select(CheckIns::getSessionId)
                        .eq(CheckIns::getUserId, studentUid)
                        .in(CheckIns::getSessionId, candidateSessionIds)))
                .orElse(Collections.emptyList())
                .stream()
                .map(CheckIns::getSessionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        CheckInSession target = null;
        boolean hasAllowed = false;
        boolean hasCheckedAllowed = false;
        for (CheckInSession s : sessions) {
            if (s == null || s.getId() == null) {
                continue;
            }
            if (!allowedSessionIds.contains(s.getId())) {
                continue;
            }
            hasAllowed = true;
            if (checkedSessionIds.contains(s.getId())) {
                hasCheckedAllowed = true;
                continue;
            }
            target = s;
            break;
        }
        if (target == null) {
            if (hasAllowed && hasCheckedAllowed) {
                AssertUtil.isTrue(false, "已签到");
            }
            AssertUtil.isTrue(false, "签到码无效或已过期");
        }

        CheckIns record = new CheckIns();
        record.setUserId(studentUid);
        record.setCourseId(target.getCourseId());
        record.setSessionId(target.getId());
        record.setClassId(classId);
        record.setCheckInTime(now);
        record.setLocation("签到码");
        record.setLatitude(null);
        record.setLongitude(null);
        record.setAccuracy(null);
        record.setStatus(1);
        record.setCreateTime(now);
        record.setUpdateTime(now);
        checkInsMapper.insert(record);

        StudentCheckInResp resp = new StudentCheckInResp();
        resp.setCheckInId(record.getId());
        resp.setSessionId(target.getId());
        resp.setDistanceMeters(null);
        resp.setCheckInTime(toMillis(now));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentCheckInResp studentCheckInByQrCode(Long studentUid, StudentCheckInQrCodeReq req) {
        assertStudent(studentUid);
        AssertUtil.isNotEmpty(req, "请求不能为空");
        AssertUtil.isNotEmpty(req.getContent(), "二维码内容不能为空");

        String content = req.getContent() == null ? null : req.getContent().trim();
        AssertUtil.isNotEmpty(content, "二维码内容不能为空");

        String token;
        if (content.startsWith(CHECKIN_QR_PREFIX)) {
            token = content.substring(CHECKIN_QR_PREFIX.length()).trim();
        } else {
            token = content;
        }
        AssertUtil.isNotEmpty(token, "二维码无效或已过期");

        LocalDateTime now = LocalDateTime.now();

        String tokenKey = RedisKey.getKey(RedisKey.CHECKIN_QR_TOKEN_SESSION, token);
        String sessionIdStr = RedisUtils.getStr(tokenKey);
        AssertUtil.isTrue(sessionIdStr != null && !sessionIdStr.isBlank(), "二维码无效或已过期");
        Long sessionId;
        try {
            sessionId = Long.parseLong(sessionIdStr.trim());
        } catch (Exception e) {
            sessionId = null;
        }
        AssertUtil.isNotEmpty(sessionId, "二维码无效或已过期");

        Long classId = getStudentClassId(studentUid);
        AssertUtil.isNotEmpty(classId, "未找到学生所属班级");

        CheckInSession session = checkInSessionMapper.selectById(sessionId);
        AssertUtil.isNotEmpty(session, "签到不存在");
        AssertUtil.isTrue(Objects.equals(session.getStatus(), 1), "签到已结束");
        AssertUtil.isTrue(session.getStartTime() != null, "签到时间配置异常");
        LocalDateTime effectiveEndTime = resolveEffectiveEndTime(session);
        AssertUtil.isTrue(effectiveEndTime != null, "签到时间配置异常");
        AssertUtil.isTrue(!now.isBefore(session.getStartTime()) && !now.isAfter(effectiveEndTime), "不在签到有效时间内");

        long relCount = checkInSessionClassMapper.selectCount(new LambdaQueryWrapper<CheckInSessionClass>()
                .eq(CheckInSessionClass::getSessionId, session.getId())
                .eq(CheckInSessionClass::getClassId, classId));
        AssertUtil.isTrue(relCount > 0, "不在本次签到班级范围内");

        long existCount = checkInsMapper.selectCount(new LambdaQueryWrapper<CheckIns>()
                .eq(CheckIns::getUserId, studentUid)
                .eq(CheckIns::getSessionId, session.getId()));
        AssertUtil.isTrue(existCount == 0, "已签到");

        CheckIns record = new CheckIns();
        record.setUserId(studentUid);
        record.setCourseId(session.getCourseId());
        record.setSessionId(session.getId());
        record.setClassId(classId);
        record.setCheckInTime(now);
        record.setLocation("扫码");
        record.setLatitude(null);
        record.setLongitude(null);
        record.setAccuracy(null);
        record.setStatus(1);
        record.setCreateTime(now);
        record.setUpdateTime(now);
        checkInsMapper.insert(record);

        StudentCheckInResp resp = new StudentCheckInResp();
        resp.setCheckInId(record.getId());
        resp.setSessionId(session.getId());
        resp.setDistanceMeters(null);
        resp.setCheckInTime(toMillis(now));
        return resp;
    }

    private void assertTeacher(Long uid) {
        Users user = userInfoCache.get(uid);
        AssertUtil.isNotEmpty(user, "用户不存在");
        AssertUtil.isTrue(user.getRole() != null && user.getRole() == 2, "仅教师可操作");
    }

    private void assertStudent(Long uid) {
        Users user = userInfoCache.get(uid);
        AssertUtil.isNotEmpty(user, "用户不存在");
        AssertUtil.isTrue(user.getRole() != null && user.getRole() == 1, "仅学生可操作");
    }

    private void assertTeacherOwnsCourse(Long teacherUid, Long courseId) {
        long count = teacherCourseMapper.selectCount(new LambdaQueryWrapper<TeacherCourse>()
                .eq(TeacherCourse::getTeacherUid, teacherUid)
                .eq(TeacherCourse::getCourseId, courseId));
        AssertUtil.isTrue(count > 0, "该课程不属于当前教师");
    }

    private void assertCourseContainsClasses(Long courseId, List<Long> classIds) {
        long count = courseClassMapper.selectCount(new LambdaQueryWrapper<CourseClass>()
                .eq(CourseClass::getCourseId, courseId)
                .in(CourseClass::getClassId, classIds));
        AssertUtil.isTrue(count == classIds.size(), "所选班级不属于该课程");
    }

    private Long getStudentClassId(Long uid) {
        Users user = userInfoCache.get(uid);
        AssertUtil.isNotEmpty(user, "用户不存在");
        UserClassRel rel = userClassRelMapper.selectOne(new LambdaQueryWrapper<UserClassRel>().eq(UserClassRel::getUid, uid));
        if (rel != null && rel.getClassId() != null) {
            return rel.getClassId();
        }
        return user.getClassId();
    }

    private Long toMillis(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private LocalDateTime resolveEffectiveEndTime(CheckInSession session) {
        if (session == null) {
            return null;
        }
        LocalDateTime effectiveEndTime = session.getEndTime();
        if (effectiveEndTime == null && session.getStartTime() != null && session.getDurationMinutes() != null) {
            effectiveEndTime = session.getStartTime().plusMinutes(session.getDurationMinutes().longValue());
        }
        return effectiveEndTime;
    }

    private Double round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    private double haversineMeters(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        AssertUtil.isNotEmpty(lat1, "中心纬度缺失");
        AssertUtil.isNotEmpty(lon1, "中心经度缺失");
        double latRad1 = Math.toRadians(lat1.doubleValue());
        double latRad2 = Math.toRadians(lat2.doubleValue());
        double deltaLat = latRad2 - latRad1;
        double deltaLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(latRad1) * Math.cos(latRad2) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371000.0 * c;
    }

    private String buildQrPngDataUrl(String content) {
        AssertUtil.isNotEmpty(content, "二维码内容不能为空");
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 320, 320, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(MatrixToImageWriter.toBufferedImage(matrix), "PNG", out);
            String base64 = Base64.getEncoder().encodeToString(out.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (WriterException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
