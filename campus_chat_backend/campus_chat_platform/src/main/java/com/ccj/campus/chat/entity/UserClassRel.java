package com.ccj.campus.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_class")
public class UserClassRel implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long uid;
    private Long classId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
