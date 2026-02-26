package com.ccj.campus.chat.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccj.campus.chat.entity.Users;
import com.ccj.campus.chat.mapper.UsersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author CCJ
 * @since 2024-04-27
 */
@Service
@RequiredArgsConstructor
public class UserDao extends ServiceImpl<UsersMapper, Users> {

    private final UsersMapper userMapper;

    public Users getByAccountNumber(String accountNumber) {
        return userMapper.selectOne(new LambdaQueryWrapper<Users>().eq(Users::getAccountNumber, accountNumber));
    }

    public String getFullNameByUid(Long uid) {
        return lambdaQuery().eq(Users::getId, uid).select(Users::getFullName).one().getFullName();
    }

    public Long getHeadTeacher(Long userId) {
        return userMapper.getHeadTeacher(userId);
    }

//    public CursorPageBaseResp<GroupMemberListResp> cursorPage(CursorPageBaseReq req, UserActiveStatusEnum activeStatusEnum, List<Long> membersUid) {
//        String initCursor = Optional.ofNullable(req.getCursor()).map(cursor -> {
//            if (cursor.equals("null")) {
//                return null;
//            }
//            return DateUtil.timeStampToDate(Long.parseLong(req.getCursor()));
//        }).orElse(null);
//
//        List<GroupMemberListResp> list = userMapper.cursorPage(req.getPageSize(), initCursor, activeStatusEnum.getCode(), membersUid);
//
//        //没有数据了
//        if (list.size() == 0) {
//            return new CursorPageBaseResp<>(req.getCursor(), Boolean.TRUE, list);
//        }
//
//        //还有数据
//        Boolean isLast = Boolean.FALSE;
//        Date lastTimeCursor = Optional.ofNullable(CollectionUtils.lastElement(list)).map(GroupMemberListResp::getLastOptTime).orElse(null);
//
//        //多获取一条来判断是否是最后一一条
//        GroupMemberListResp nextOne = userMapper.getGroupMemberListRespOne(lastTimeCursor, activeStatusEnum.getCode(), membersUid);
//        if (nextOne == null) {
//            isLast = Boolean.TRUE;
//        }
//
//        String cursor = Optional.ofNullable(lastTimeCursor).map(Date::getTime).map(String::valueOf).orElse(null);
//
//        return new CursorPageBaseResp<>(cursor, isLast, list);
//    }
//
//    public List<String> getUserAvatar(List<Long> uids) {
//        return userMapper.getUserAvatar(uids);
//    }
}
