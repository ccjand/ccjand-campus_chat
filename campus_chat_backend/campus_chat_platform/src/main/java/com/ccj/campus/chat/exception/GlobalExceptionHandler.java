package com.ccj.campus.chat.exception;

import com.ccj.campus.chat.dto.ApiResult;
import com.ccj.campus.chat.frequencycontrol.FrequencyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * @Author ccj
 * @Date 2024-04-10 15:35
 * @Description
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ApiResult<?> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        StringBuilder errorMsg = new StringBuilder();
        e.getFieldErrors().forEach(f -> errorMsg.append(f.getField()).append(f.getDefaultMessage()).append(" & "));
        String message = errorMsg.toString();
        message = message.substring(0, message.length() - 3);

        return ApiResult.fail(CommonErrorEnum.INVALID_PARAM.getCode(), message);
    }

    @ExceptionHandler({BusinessException.class})
    public ApiResult<?> businessException(BusinessException e) {
        log.info("业务异常:{}", e.getMessage());
        return ApiResult.fail(e.getErrorCode(), e.getErrorMsg());
    }

    @ExceptionHandler({FrequencyException.class})
    public ApiResult<?> frequencyException(FrequencyException e) {
        log.info("限流:{}", e.getMessage());
        return ApiResult.fail(e.getErrorCode(), e.getErrorMsg());
    }

    @ExceptionHandler({BindException.class})
    public ApiResult<?> bindException(BindException e) {
        StringBuilder errorMsg = new StringBuilder();
        e.getFieldErrors().forEach(f -> errorMsg.append(f.getField()).append(f.getDefaultMessage()).append(" & "));
        String message = errorMsg.toString();
        message = message.substring(0, message.length() - 3);
        return ApiResult.fail(CommonErrorEnum.INVALID_PARAM.getCode(), message);
    }

    @ExceptionHandler({DuplicateKeyException.class})
    public ApiResult<?> duplicateKeyException(DuplicateKeyException e) {
        log.info("重复数据:{}", e.getMessage());
        String msg = e.getMessage();
        if (msg != null) {
            String lower = msg.toLowerCase();
            if ((lower.contains("checkin") || lower.contains("check_in")) && lower.contains("code")) {
                return ApiResult.fail(CommonErrorEnum.BUSINESS_ERROR.getCode(), "存在相同签到码");
            }
        }
        return ApiResult.fail(CommonErrorEnum.BUSINESS_ERROR.getCode(), "请勿重复提交");
    }

    /**
     * 异常处理的兜底，避免有漏掉的异常
     *
     * @param e 最大的异常
     * @return 返回统一的异常信息结构给前端
     */
    @ExceptionHandler(Throwable.class)
    public ApiResult<?> throwable(Throwable e) {
        log.error("system server error, the reason is {}", e.getMessage(), e);
        return ApiResult.fail(CommonErrorEnum.SYSTEM_ERROR);
    }


}
