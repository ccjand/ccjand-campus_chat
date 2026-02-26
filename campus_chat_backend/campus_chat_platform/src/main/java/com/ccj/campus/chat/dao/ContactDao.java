package com.ccj.campus.chat.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccj.campus.chat.dto.CursorPageBaseReq;
import com.ccj.campus.chat.dto.CursorPageBaseResp;
import com.ccj.campus.chat.entity.Contact;
import com.ccj.campus.chat.entity.Messages;
import com.ccj.campus.chat.imservice.domain.vo.req.ChatMessageReadReq;
import com.ccj.campus.chat.mapper.ContactMapper;
import com.ccj.campus.chat.utils.CursorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

/**
 * <p>
 * 会话列表 服务实现类
 * </p>
 *
 * @author CCJ
 * @since 2024-05-06
 */
@Service
@RequiredArgsConstructor
public class ContactDao extends ServiceImpl<ContactMapper, Contact> {

    private final ContactMapper contactMapper;

    public void createOrUpdateContact(Set<Long> groupMembers, Long roomId, LocalDateTime activeTime, Long messageId) {
        LocalDateTime now = LocalDateTime.now();
        List<Contact> contacts = lambdaQuery().eq(Contact::getRoomId, roomId)
                .in(Contact::getUid, groupMembers)
                .list();

        //对已有的进行更新, 对没有的会话进行创建
        if (contacts.size() > 0) {
            // 更新
            List<Contact> updateList = contacts.stream().map(contact -> {
                Contact update = new Contact();
                update.setId(contact.getId());
                update.setActiveTime(activeTime);
                update.setLastMsgId(messageId);
                update.setUpdateTime(now);
                return update;
            }).collect(Collectors.toList());

            //批量更新
            ((ContactDao) AopContext.currentProxy()).updateBatchById(updateList);
        }

        Set<Long> source = contacts.stream().map(Contact::getUid).collect(Collectors.toSet());

        //需要创建的会话
        HashSet<Long> tempSet = new HashSet<>(groupMembers);

        tempSet.removeAll(source);


        //批量插入
        //新增
        List<Contact> insertList = tempSet.stream().map(uid -> {
            Contact contact = new Contact();
            contact.setUid(uid);
            contact.setRoomId(roomId);
            contact.setActiveTime(activeTime);
            contact.setLastMsgId(messageId);
            contact.setCreateTime(now);
            contact.setUpdateTime(now);
            return contact;
        }).collect(Collectors.toList());

        //批量新增
        ((ContactDao) AopContext.currentProxy()).saveBatch(insertList);
    }


    public Contact getContact(Long uid, Long roomId) {
        return lambdaQuery().eq(Contact::getUid, uid)
                .eq(Contact::getRoomId, roomId)
                .one();
    }

    public CursorPageBaseResp<Contact> contactList(Long uid, CursorPageBaseReq pageRequest) {
        return CursorUtils.getCursorPageFromMysql(this, pageRequest,
                wrapper -> wrapper.eq(Contact::getUid, uid),
                Contact::getActiveTime);
    }

    public void updateTop(Long contactId, Integer isTop) {
        lambdaUpdate().eq(Contact::getId, contactId)
                .set(Contact::getIsTop, isTop)
                .update();
    }

    public List<Contact> contactListByRoomId(Long uid, List<Long> roomIds) {
        return lambdaQuery().eq(Contact::getUid, uid)
                .in(Contact::getRoomId, roomIds)
                .list();
    }

    public Integer getTotalCount(Long roomId) {
        return lambdaQuery()
                .eq(Contact::getRoomId, roomId)
                .count();
    }


    public CursorPageBaseResp<Contact> readPage(Messages message, ChatMessageReadReq chatMessageReadReq, Long myUid) {
        return CursorUtils.getCursorPageFromMysql(this, chatMessageReadReq,
                wrapper -> wrapper.eq(Contact::getRoomId, message.getRoomId())
                        .ne(Contact::getUid, myUid) //自己不用出现在已读和未读列表
                        .ge(Contact::getReadTime, message.getCreateTime()),
                Contact::getReadTime);
    }

    public CursorPageBaseResp<Contact> unreadPage(Messages message, ChatMessageReadReq chatMessageReadReq, Long myUid) {
        return CursorUtils.getCursorPageFromMysql(this, chatMessageReadReq,
                wrapper -> wrapper.eq(Contact::getRoomId, message.getRoomId())
                        .ne(Contact::getUid, myUid) //自己不用出现在已读和未读列表
                        .lt(Contact::getReadTime, message.getCreateTime()),
                Contact::getReadTime);
    }

    public Contact getByRoomId(Long uid, Long roomId) {
        return lambdaQuery()
                .eq(Contact::getUid, uid)
                .eq(Contact::getRoomId, roomId)
                .one();
    }

    public List<Long> getDisturbContact(Long roomId) {
        return contactMapper.getDisturbContact(roomId);
    }
}
