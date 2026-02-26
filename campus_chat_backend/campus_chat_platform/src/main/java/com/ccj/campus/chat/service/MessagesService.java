package com.ccj.campus.chat.service;

import com.ccj.campus.chat.entity.Messages;

public interface MessagesService {

    /**
     * 撤回消息的发出者
     */
    String getRecallMessageUserName(Long uid, Messages messages);

     /**
      * 更新消息
      */
     void updateById(Messages messages);

     /**
      * 根据消息id获取消息
      */
     Messages getMessageById(Long msgId);
}
