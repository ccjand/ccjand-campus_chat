package com.ccj.campus.chat.dto;

import com.ccj.campus.chat.entity.Users;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @Author ccj
 * @Date 2024-04-13 12:36
 * @Description 用户上线事件
 */
@Getter
public class UserOnlineEvent extends ApplicationEvent {

    private Users user;

    /**
     * @param source 事件的来源【订阅者可以知晓发布者是谁,从哪个类发出的】
     * @param user   当前用户的信息
     */
    public UserOnlineEvent(Object source, Users user) {
        super(source);
        this.user = user;
    }
}
