package com.ccj.campus.chat.dto;

import com.ccj.campus.chat.entity.Users;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @Author ccj
 * @Date 2024-07-02 15:46
 * @Description
 */
@Getter
public class UserOfflineEvent extends ApplicationEvent {

    private Users user;

    public UserOfflineEvent(Object source, Users user) {
        super(source);
        this.user = user;
    }
}
