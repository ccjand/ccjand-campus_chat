package com.ccj.campus.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author ccj
 * @Date 2026-01-16 16:50
 * @Description
 */
@Data
@TableName("leave_request")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 请假类型：1病假 2事假 3其他
     */
    private Integer leaveType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 请假时长（天）
     */
    private Integer durationDays;

    /**
     * 课程id
     */
    private String courseId;

    /**
     * 请假原因
     */
    private String reason;

    /**
     * 状态：0待审批 1已通过 2已驳回 3已撤销
     */
    private Integer status;

    /**
     * 审批人用户id
     */
    private Long approverId;

    /**
     * 审批意见
     */
    private String approverComment;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 附件类型：1图片 2文件
     */
    private Integer attachmentType;

    /**
     * 附件url
     */
    private String attachmentUrl;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
