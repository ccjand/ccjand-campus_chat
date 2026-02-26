package com.ccj.campus.chat.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("check_in_session")
public class CheckInSession implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long teacherUid;

    private String title;

    private BigDecimal centerLatitude;

    private BigDecimal centerLongitude;

    private Integer radiusMeters;

    private Integer durationMinutes;

    private LocalDateTime startTime;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime endTime;

    private Integer status;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
