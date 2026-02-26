package com.ccj.campus.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("users")
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @TableField(exist = false)
    public static final Long SYSTEM_ID = 100L; // 系统消息的uid

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String accountNumber;

    private String fullName;

    private String avatar;

    private Integer sex;

    private Integer activeStatus;

    private LocalDateTime lastOptTime;

    private Integer status;

    private String phone;

    private String password;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer role;

    private Long departmentId;

    /**
     * 如果是教师，表示管理的班级（班主任）；如果是学生，表示所属的班级
     */
    private Long classId;
}

