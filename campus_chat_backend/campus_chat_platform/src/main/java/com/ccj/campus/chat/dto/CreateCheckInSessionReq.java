package com.ccj.campus.chat.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateCheckInSessionReq implements Serializable {
    @NotNull(message = "课程不能为空")
    private Long courseId;

    private String title;

    @NotNull(message = "中心纬度不能为空")
    private BigDecimal centerLatitude;

    @NotNull(message = "中心经度不能为空")
    private BigDecimal centerLongitude;

    @NotNull(message = "签到半径不能为空")
    private Integer radiusMeters;

    @NotNull(message = "有效时长不能为空")
    private Integer durationMinutes;

    @NotEmpty(message = "班级不能为空")
    private List<Long> classIds;
}
