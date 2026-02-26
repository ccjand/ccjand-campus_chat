package com.ccj.campus.chat.imservice.domain.vo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @Author ccj
 * @Date 2024-05-07 15:38
 * @Description 聊天消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageReq {


    //聊天室id
    @NotNull
    private Long roomId;

    /**
     * @see com.ccj.campus.chat.enums.MessageTypeEnum
     */
    //消息类型
    @NotNull
    private Integer msgType;

    //消息序列号(单聊需要客户端自己上传, 需要递增, 群聊传递任意值)
    @NotNull
    private Long msgSeq;

    /**
     * 单聊根据 发送者uid+msgSeq+roomId+timestamp+(客户端生成)+random去重
     * 群聊根据 random+发送者uid去重
     */
    //随机数, 用来去重(单聊|群聊会在缓冲区里面去重)
    @NotNull
    private Integer random;


    //消息时间戳（前端不需要传递, 后端会自己覆盖。单位是秒）
    @Nullable
    private Long timestamp;

    @Nullable
    @Size(max = 64)
    private String clientMsgId;


    //消息内容, 不同的消息类型对应不用的消息内容,格式不固定
    @NotNull
    private Object msgContent;
}
