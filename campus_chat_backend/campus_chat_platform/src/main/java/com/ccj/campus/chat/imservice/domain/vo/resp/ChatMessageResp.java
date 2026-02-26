package com.ccj.campus.chat.imservice.domain.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author ccj
 * @Date 2024-05-07 15:41
 * @Description 聊天消息返回内容
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessageResp {

    //发送者的基本信息
    private UserInfo fromUser;

    //消息详情内容
    private Message message;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long uid;
        private String name;
        private String avatar;
    }

    @Data
    public static class Message {
        //消息id
        private Long id;
        //房间id
        private Long roomId;
        //消息序列号(发送方生成的)
        private Long msgSeq;
        //消息发送时间
        private LocalDateTime sendTime;
        //消息类型 1正常文本 2.撤回消息
        private Integer type;
        private String clientMsgId;
        //消息内容不同的消息类型，内容体不同
        private Object messageContent;
    }
}
