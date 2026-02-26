package com.ccj.campus.chat.entity;

import com.ccj.campus.chat.dto.FileMessageDto;
import com.ccj.campus.chat.dto.ImageMessageDto;
import com.ccj.campus.chat.dto.RecallMessageDto;
import com.ccj.campus.chat.dto.VideoMessageDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @Author ccj
 * @Date 2024-05-09 21:25
 * @Description 消息扩展内容
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageExtra implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String clientMsgId;

    /**
     * 撤回消息详情
     */
    private RecallMessageDto recallMessage;
    /**
     * 艾特的用户uid
     */
    private List<Long> atUidList;

    /**
     * 艾特全部用户
     */
    private Boolean atAllUser;
    /**
     * 文件消息
     */
    private FileMessageDto fileMessage;
    /**
     * 图片消息
     */
    private ImageMessageDto imageMessage;

    /**
     * 本地视频消息
     */
    private VideoMessageDto videoMessage;
}
