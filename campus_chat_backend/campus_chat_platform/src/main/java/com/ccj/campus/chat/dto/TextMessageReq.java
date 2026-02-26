package com.ccj.campus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @Author ccj
 * @Date 2024-05-09 21:07
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
//文本消息
public class  TextMessageReq {

    //消息内容
    @NotNull
    @Size(min = 1, max = 1024, message = "文本长度太长")
    private String content;

    //回复的消息id, 如果没有就传递null
    private Long replyMessageId;

    //被艾特的用户id
    @Size(max = 10, message = "一次只能艾特10人哦")
    private List<Long> atUidList;

    //是否艾特全体成员
    private Boolean atAllUser;
}
