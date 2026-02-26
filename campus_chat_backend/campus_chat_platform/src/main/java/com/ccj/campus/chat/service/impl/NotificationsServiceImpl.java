package com.ccj.campus.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccj.campus.chat.entity.Notifications;
import com.ccj.campus.chat.mapper.NotificationsMapper;
import com.ccj.campus.chat.service.NotificationsService;
import org.springframework.stereotype.Service;

@Service
public class NotificationsServiceImpl extends ServiceImpl<NotificationsMapper, Notifications> implements NotificationsService {
}
