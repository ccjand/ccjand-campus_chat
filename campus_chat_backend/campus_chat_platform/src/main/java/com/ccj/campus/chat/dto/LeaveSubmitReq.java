package com.ccj.campus.chat.dto;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class LeaveSubmitReq implements Serializable {
    @NotNull(message = "请假类型不能为空")
    private Integer leaveType;
    @NotNull(message = "开始时间不能为空")
    private Long startTime; // timestamp
    @NotNull(message = "结束时间不能为空")
    private Long endTime; // timestamp
    @NotNull(message = "请假理由不能为空")
    private String reason;
    private String className;
    
    private List<AttachmentItem> attachments;
    
    @Data
    public static class AttachmentItem {
        private Integer fileType; // 1文件 2图片
        private String fileUrl;
        private String fileName;
        private Long fileSize;
    }
}
