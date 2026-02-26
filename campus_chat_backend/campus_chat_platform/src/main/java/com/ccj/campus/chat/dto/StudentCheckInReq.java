package com.ccj.campus.chat.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class StudentCheckInReq implements Serializable {
    @NotNull(message = "sessionId不能为空")
    private Long sessionId;

    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;

    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;

    private Integer accuracy;

    private String location;
}
