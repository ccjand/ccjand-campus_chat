package com.ccj.campus.chat.interceptor;

import com.ccj.campus.chat.enums.TerminalTypeEnum;
import com.ccj.campus.chat.frequencycontrol.entity.dto.RequestHolderInfo;
import com.ccj.campus.chat.util.RequestHolder;
import com.ccj.campus.chat.utils.AssertUtil;
import com.ccj.campus.chat.utils.JwtUtils;
import com.ccj.campus.chat.utils.RedisKey;
import com.ccj.campus.chat.utils.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author ccj
 * @Date 2024-05-04 22:16
 * @Description 收集当前登录用户的相关信息
 */
@Component
public class CollectionInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String AUTHORIZATION_SCHEMA = "Bearer ";
    public static final String UID = "uid";
    public static final String TERMINAL_TYPE = "terminalType";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader(HEADER_AUTHORIZATION);
        //分割出token
        AssertUtil.isNotEmpty(authorization, "Authorization 不能为空");
        AssertUtil.isTrue(authorization.startsWith(AUTHORIZATION_SCHEMA), "Authorization 格式错误");
        String[] split = authorization.split(" ");
        AssertUtil.isTrue(split.length == 2, "Authorization 格式错误");

        Integer terminalType = Integer.valueOf(request.getHeader(TERMINAL_TYPE));
        AssertUtil.isTrue(TerminalTypeEnum.support(terminalType), "terminalType 格式错误");

        String token = split[1];
        //获取对应的uid
        Long uid = getValidUid(token, terminalType);
        if (uid == null) {
            response.setStatus(401);
            return false;
        }

        RequestHolderInfo info = new RequestHolderInfo(uid, terminalType);
        RequestHolder.set(info);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //避免内存溢出
        RequestHolder.remove();
    }

    public Long getValidUid(String token, Integer terminalType) {
        Long uid = jwtUtils.getUidOrNull(token);
        if (uid == null) {
            return null;
        }

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

    private String getUserTokenKey(Long uid, Integer terminalType) {
        return RedisKey.getKey(RedisKey.USER_TOKEN, uid, terminalType);
    }

}
