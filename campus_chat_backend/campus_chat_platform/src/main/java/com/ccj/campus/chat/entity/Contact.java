package com.ccj.campus.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("contact")
public class Contact implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long uid;

    private Long roomId;

    private LocalDateTime readTime;

    private LocalDateTime activeTime;

    private Long lastMsgId;

    private Integer isTop;

    private Integer notDisturb;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
