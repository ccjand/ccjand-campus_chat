package com.ccj.campus.chat.strategy;

import cn.hutool.core.bean.BeanUtil;
import com.ccj.campus.chat.dao.MessageDao;
import com.ccj.campus.chat.entity.Messages;
import com.ccj.campus.chat.enums.MessageTypeEnum;
import com.ccj.campus.chat.imservice.domain.vo.req.ChatMessageReq;
import com.ccj.campus.chat.imservice.service.MessageHandlerFactory;
import com.ccj.campus.chat.imservice.service.adapter.MessageAdapter;
import com.ccj.campus.chat.service.impl.IDService;
import com.ccj.campus.chat.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;

/**
 * @Author ccj
 * @Date 2024-05-07 16:44
 * @Description
 */
public abstract class AbstractMessageHandler<MsgType> {

    private Class<MsgType> bodyClass;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private IDService idService;

    @PostConstruct
    public void init() {
        ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.bodyClass = (Class<MsgType>) genericSuperclass.getActualTypeArguments()[0];
        MessageHandlerFactory.register(getMsgTypeEnum().getType(), this);
    }


    /**
     * 展示消息
     */
    public abstract Object showMsg(Messages msg);

    /**
     * 被回复时——展示的消息
     */
    public abstract Object showReplyMsg(Messages msg);


    /**
     * 会话列表——展示的消息
     */
    public abstract String showContactMsg(Long uid, Messages msg);

    public abstract MessageTypeEnum getMsgTypeEnum();

    public abstract void saveMessage(Messages message, MsgType messageContent);


    protected void check(Long uid, MsgType messageContent, Long roomId) {
    }


    @Transactional(rollbackFor = Exception.class)
    public Long checkAndSaveMessage(Long uid, ChatMessageReq chatMessageReq) {
        MsgType messageContent = toMessageBean(chatMessageReq.getMsgContent());
        //统一校验【注解校验】
        AssertUtil.allCheckValidateThrow(messageContent);
        //子类可扩展对消息的校验
        check(uid, messageContent, chatMessageReq.getRoomId());
        //统一保存部分信息
        Messages insert = MessageAdapter.buildMessageSave(uid, chatMessageReq);
        Long messageId = idService.getMessageId();
        if (messageId != null) {
            insert.setId(messageId);
        }
        insert.setMsgSeq(chatMessageReq.getMsgSeq());
        messageDao.save(insert); //让子类来保存, 不然要和db交互两次,子类来保存就只需要一次
        //子类扩展保存逻辑, 补充并修改信息
        saveMessage(insert, messageContent);
        return messageId != null ? messageId : insert.getId();
    }


    private MsgType toMessageBean(Object content) {
        if (content.getClass().isAssignableFrom(bodyClass)) {
            return (MsgType) content;
        }

        return BeanUtil.toBean(content, bodyClass);
    }

}
