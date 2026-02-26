package com.ccj.campus.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author ccj
 * @Date 2024-05-10 14:43
 * @Description
 */
@Data
//文本消息响应对象
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextMessageResp {

    //文本消息内容
    private String content;

    //@用户列表
    private List<Long> atUidList;

    //回复消息, 没有的话就是null
    private TextMessageResp.ReplyMessage replyMessage;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReplyMessage {

        //当前消息的id
        private Long id;

        //发送者的用户uid
        private Long uid;

        //发送者的用户名称
        private String username;

        /**
         * @see com.ccj.campus.chat.enums.MessageTypeEnum
         */
        //消息类型 1正常文本 2.撤回消息
        private Integer type;

        //消息内容不同的消息类型，见父消息内容体
        private Object body;

        //是否可回调消息跳转 0否 1是, 就像微信一样, 点击就可以定位你回复的那条消息的位置
        private Integer canCallback;

        //跳转间隔的消息条数
        private Integer gapCount;
    }
}
