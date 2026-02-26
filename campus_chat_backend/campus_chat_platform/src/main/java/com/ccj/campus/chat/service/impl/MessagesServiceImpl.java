package com.ccj.campus.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccj.campus.chat.dao.UserDao;
import com.ccj.campus.chat.entity.Messages;
import com.ccj.campus.chat.frequencycontrol.entity.dto.RequestHolderInfo;
import com.ccj.campus.chat.mapper.MessagesMapper;
import com.ccj.campus.chat.service.MessagesService;
import com.ccj.campus.chat.util.RequestHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MessagesServiceImpl implements MessagesService {

    @Resource
    private UserDao userDao;

    @Resource
    private MessagesMapper messagesMapper;



    @Override
    public String getRecallMessageUserName(Long uid, Messages messages) {
        if (messages.getFromUid().equals(uid)) {
            return "我";
        }

        return userDao.getFullNameByUid(messages.getFromUid());
    }

    @Override
    public void updateById(Messages messages) {
        messagesMapper.updateById(messages);
    }

    @Override
    public Messages getMessageById(Long msgId) {
        return messagesMapper.selectById(msgId);
    }
}
