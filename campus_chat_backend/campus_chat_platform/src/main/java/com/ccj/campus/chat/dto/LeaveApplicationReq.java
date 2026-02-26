package com.ccj.campus.chat.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author ccj
 * @Date 2026-01-16 16:58
 * @Description
 */
@Data
public class LeaveApplicationReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @NotNull(message = "用户id不能为空")
    private Long userId;

    /**
     * 请假类型：1病假 2事假 3其他
     */
    @NotNull(message = "请假类型不能为空")
    private Integer leaveType;
    /**
     * 请假开始时间
     */
    @NotNull(message = "请假开始时间不能为空")
    private LocalDateTime startTime;

    /**
     * 请假结束时间
     */
    @NotNull(message = "请假结束时间不能为空")
    private LocalDateTime endTime;

    /**
     * 请假时长（天）
     */
    @NotNull(message = "请假时长不能为空")
    private Integer durationDays;

    /**
     * 课程id
     */
    @NotNull(message = "课程id不能为空")
    private Long courseId;

    /**
     * 请假原因
     */
    private String reason;

    /**
     * 附件类型：1图片 2文件
     */
    private Integer attachmentType;

    /**
     * 附件文件
     */
    private MultipartFile file;
}
