package com.ccj.campus.chat.exception;


import com.ccj.campus.chat.dto.ApiResult;
import com.ccj.campus.chat.util.JsonUtils;
import lombok.AllArgsConstructor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author ccj
 * @Date 2024-04-08 15:41
 * @Description
 */
@AllArgsConstructor
public enum HttpErrorEnum implements ErrorEnum {

    ACCESS_DENIED(401, "未登录或无权限操作!");

    private final Integer httpCode;
    private final String desc;

    public void sendError(HttpServletResponse response) throws IOException {
        response.setStatus(httpCode);
        response.setContentType("application/json;charset=UTF-8");
        String errorDetail = JsonUtils.toStr(ApiResult.fail(httpCode, desc));
        response.getWriter().write(errorDetail);
    }

    @Override
    public Integer getErrorCode() {
        return httpCode;
    }

    @Override
    public String getErrorMsg() {
        return desc;
    }
}
