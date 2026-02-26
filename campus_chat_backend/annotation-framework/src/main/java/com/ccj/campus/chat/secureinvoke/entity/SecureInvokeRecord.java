package com.ccj.campus.chat.secureinvoke.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ccj.campus.chat.secureinvoke.entity.dto.SecureInvokeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 本地消息表
 *
 * @TableName secure_invoke_record
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "secure_invoke_record", autoResultMap = true)
public class SecureInvokeRecord implements Serializable {

    public static final Integer STATUE_WAIT = 1;//等待执行
    public static final Integer STATUE_FAIL = 2;//执行失败

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 请求快照参数json
     */
    @TableField(value = "secure_invoke_json", typeHandler = JacksonTypeHandler.class)
    private SecureInvokeDTO secureInvokeJson;

    /**
     * 状态: 1待执行 2已失败
     */
    @TableField(value = "status")
    @Builder.Default
    private Integer status = STATUE_WAIT;

    /**
     * 下一次重试的时间
     */
    @TableField(value = "next_retry_time")
    @Builder.Default
    private LocalDateTime nextRetryTime = LocalDateTime.now();

    /**
     * 已经重试的次数
     */
    @TableField(value = "retry_times")
    @Builder.Default
    private Integer retryTimes = 0;

    /**
     * 支持的最大重试次数
     */
    @TableField(value = "max_retry_times")
    private Integer maxRetryTimes;

    /**
     * 失败原因
     */
    @TableField(value = "fail_reason")
    private String failReason;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}
