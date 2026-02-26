package com.ccj.campus.chat.frequencycontrol;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 自定义限流异常
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FrequencyException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    protected Integer errorCode;

    /**
     * 错误信息
     */
    protected String errorMsg;


    public FrequencyException(String errorMsg) {
        super(errorMsg);
        this.errorMsg = errorMsg;
    }

    public FrequencyException(Integer code, String errorMsg) {
        this.errorCode = code;
        this.errorMsg = errorMsg;
    }
}
