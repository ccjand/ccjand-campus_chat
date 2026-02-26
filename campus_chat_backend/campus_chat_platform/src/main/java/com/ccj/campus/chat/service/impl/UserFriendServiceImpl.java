package com.ccj.campus.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccj.campus.chat.dto.CursorPageBaseReq;
import com.ccj.campus.chat.dto.CursorPageBaseResp;
import com.ccj.campus.chat.entity.UserFriend;
import com.ccj.campus.chat.enums.DeleteStatusEnum;
import com.ccj.campus.chat.mapper.UserFriendMapper;
import com.ccj.campus.chat.utils.CursorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
public class UserFriendServiceImpl extends ServiceImpl<UserFriendMapper, UserFriend> {

    private final UserFriendMapper userFriendMapper;


    /**
     * 游标翻页【优化了 普通翻页演化为深翻页的性能问题】
     * 例: select * from user_friend where uid = 1001 and id < 100 order by id desc limit 0, 5
     *
     * @param uid 当前登录用户 uid
     * @param req 游标参数 {@link CursorPageBaseReq}
     */
    public CursorPageBaseResp<UserFriend> friendPage(Long uid, CursorPageBaseReq req) {
        return CursorUtils.getCursorPageFromMysql(this, req,
                wrapper -> wrapper.eq(UserFriend::getUid, uid)
                        .eq(UserFriend::getDeleteStatus, DeleteStatusEnum.NOT_DELETED.getStatus()),
                UserFriend::getId);
    }


    /**
     * 获取当前用户的好友列表
     */
    public List<UserFriend> getFriendsByUids(Long uid, List<Long> uidList) {
        return lambdaQuery().eq(UserFriend::getUid, uid)
                .eq(UserFriend::getDeleteStatus, DeleteStatusEnum.NOT_DELETED)
                .in(UserFriend::getFriendUid, uidList)
                .list();
    }


    public List<UserFriend> getFriendship(Long uid, Long friendUid) {
        return lambdaQuery().or(wrapper -> wrapper.eq(UserFriend::getUid, uid).eq(UserFriend::getFriendUid, friendUid))
                .or(wrapper -> wrapper.eq(UserFriend::getUid, friendUid).eq(UserFriend::getFriendUid, uid))
                .list();
    }


    public UserFriend getFriend(Long uid, Long targetId) {
        return lambdaQuery().eq(UserFriend::getUid, uid)
                .eq(UserFriend::getFriendUid, targetId)
                .eq(UserFriend::getDeleteStatus, DeleteStatusEnum.NOT_DELETED.getStatus())
                .one();
    }

    public List<UserFriend> getAllFriends(Long uid) {
        return lambdaQuery().eq(UserFriend::getUid, uid)
                .eq(UserFriend::getDeleteStatus, DeleteStatusEnum.NOT_DELETED.getStatus())
                .list();
    }
}
