package com.ccj.campus.chat.service.impl;


import com.ccj.campus.chat.cache.UserCache;
import com.ccj.campus.chat.cache.UserFriendCache;
import com.ccj.campus.chat.cache.UserInfoCache;
import com.ccj.campus.chat.dao.UserDao;
import com.ccj.campus.chat.dto.UserLoginReq;
import com.ccj.campus.chat.dto.UserLoginResp;
import com.ccj.campus.chat.entity.Departments;
import com.ccj.campus.chat.entity.Classes;
import com.ccj.campus.chat.entity.UserClassRel;
import com.ccj.campus.chat.entity.Users;
import com.ccj.campus.chat.enums.DepartmentType;
import com.ccj.campus.chat.enums.TerminalTypeEnum;
import com.ccj.campus.chat.exception.BusinessException;
import com.ccj.campus.chat.mapper.ClassesMapper;
import com.ccj.campus.chat.mapper.DepartmentsMapper;
import com.ccj.campus.chat.mapper.UserClassRelMapper;
import com.ccj.campus.chat.service.UsersService;
import com.ccj.campus.chat.util.RequestHolder;
import com.ccj.campus.chat.utils.AssertUtil;
import com.ccj.campus.chat.utils.JwtUtils;
import com.ccj.campus.chat.utils.RedisKey;
import com.ccj.campus.chat.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * @Author ccj
 * @Date 2024-05-01 14:24
 * @Description
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UserDao userDao;
    private final JwtUtils jwtUtils;
    private final UserCache userCache;
    private final UserFriendCache userFriendCache;
    private final UserInfoCache userInfoCache;
    private final DepartmentsMapper departmentsMapper;
    private final ClassesMapper classesMapper;
    private final UserClassRelMapper userClassRelMapper;

    //不能加final，会被ioc注入
    private static String TERMINAL_TYPE = "terminalType";

    @Override
    public Long getValidUid(String token) {
        Long uid = jwtUtils.getUidOrNull(token);
        if (uid == null) {
            return null;
        }

        Integer terminalType = RequestHolder.get().getTerminalType();

        String key = getUserTokenKey(uid, terminalType);
        String tokenInRedis = RedisUtils.getStr(key);
        //没有登录
        if (StringUtils.isBlank(tokenInRedis)) {
            return null;
        }

        //不能用老token来登录
        if (!token.equals(tokenInRedis)) {
            return null;
        }

        return uid;
    }

    /**
     * 登录成功, 生成token, 存储到redis
     */
    private String loginSuccessful(Long uid, Integer terminalType) {
        //根据uid生成token
        String token = jwtUtils.createToken(uid);
        //存储到redis
        String key = getUserTokenKey(uid, terminalType);
        RedisUtils.set(key, token, RedisKey.LOGIN_TOKEN_EXPIRE_TIME, TimeUnit.DAYS);//7天过期
        return token;
    }

    @Override
    public UserLoginResp login(UserLoginReq loginReq, HttpServletRequest request) {
        String terminalTypeStr = request.getHeader(TERMINAL_TYPE);
        AssertUtil.isNotEmpty(terminalTypeStr, "终端类型不能为空");

        Integer terminalType = Integer.valueOf(terminalTypeStr);
        if (!TerminalTypeEnum.support(terminalType)) {
            throw new BusinessException("不支持的终端类型");
        }

        Users find = userDao.getByAccountNumber(loginReq.getAccountNumber());
        AssertUtil.isNotEmpty(find, "账号不存在");
        //登录成功
        String token = loginSuccessful(find.getId(), terminalType);

        userInfoCache.online(find.getId());

        String className = null;
        if (find.getRole() != null && find.getRole() == 1) {
            Long classId = null;
            UserClassRel rel = userClassRelMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserClassRel>()
                    .eq(UserClassRel::getUid, find.getId()));
            if (rel != null) {
                classId = rel.getClassId();
            }
            if (classId == null) {
                classId = find.getClassId();
            }
            if (classId != null) {
                Classes cls = classesMapper.selectById(classId);
                if (cls != null) {
                    className = cls.getName();
                }
            }
        }

        return UserLoginResp.builder()
                .uid(find.getId())
                .accountNumber(find.getAccountNumber())
                .avatar(find.getAvatar())
                .token(token)
                .role(find.getRole())
                .fullName(find.getFullName())
                .department(DepartmentType.getDepartmentName(find.getDepartmentId()))
                .className(className)
                .build();
    }

    @Override
    public void logout(Long uid) {
        Integer terminalType = RequestHolder.get().getTerminalType();
        String key = getUserTokenKey(uid, terminalType);
        RequestHolder.remove();
        //删除用户信息缓存
        userCache.delUserInfo(uid);
        //删除登录token
        RedisUtils.del(key);
    }


    private String getUserTokenKey(Long uid, Integer terminalType) {
        return RedisKey.getKey(RedisKey.USER_TOKEN, uid, terminalType);
    }

}
