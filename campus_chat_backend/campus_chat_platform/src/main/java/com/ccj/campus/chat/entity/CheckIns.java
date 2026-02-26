package com.ccj.campus.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("check_ins")
public class CheckIns implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDateTime checkInTime;

    private String location;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long courseId;

    private Long sessionId;

    private Long classId;

    private Integer accuracy;
}
