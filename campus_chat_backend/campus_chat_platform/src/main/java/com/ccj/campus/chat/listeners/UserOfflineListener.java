package com.ccj.campus.chat.listeners;


import com.ccj.campus.chat.cache.GroupMemberCache;
import com.ccj.campus.chat.cache.UserCache;
import com.ccj.campus.chat.config.ExtRocketMQTemplate;
import com.ccj.campus.chat.dao.GroupMemberDao;
import com.ccj.campus.chat.dao.UserDao;
import com.ccj.campus.chat.dto.UserOfflineEvent;
import com.ccj.campus.chat.entity.Users;
import com.ccj.campus.chat.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;

/**
 * @Author ccj
 * @Date 2024-04-13 12:44
 * @Description
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserOfflineListener {
    private final UserDao userDao;
    private final ExtRocketMQTemplate extRocketMQTemplate;
    private final UserCache userCache;
    private final GroupMemberDao groupMemberDao;
    private final GroupMemberCache groupMemberCache;

    @TransactionalEventListener(classes = UserOfflineEvent.class, phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void saveDB(UserOfflineEvent event) {
        Users user = event.getUser();
        userDao.updateById(user);
    }
    
    @TransactionalEventListener(classes = UserOfflineEvent.class, phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void refreshCache(UserOfflineEvent event) {
        Users user = event.getUser();
        userCache.userInfoChange(user.getId());
        //用户下线
        RedisUtils.executeScript("/lua/updateUserOnlineStatus.lua", Collections.emptyList(), user.getId().toString(), "0");
    }
}
