package com.ccj.campus.chat.secureinvoke.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author ccj
 * @Date 2024-05-14 19:19
 * @Description
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SecureInvokeDTO {
    private String className;
    private String methodName;
    private String parameters;
    private String args;
}
