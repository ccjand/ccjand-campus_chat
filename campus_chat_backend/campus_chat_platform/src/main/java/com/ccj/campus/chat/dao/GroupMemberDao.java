package com.ccj.campus.chat.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccj.campus.chat.dto.GetGroupMemberBaseReq;
import com.ccj.campus.chat.dto.GetGroupMemberReq;
import com.ccj.campus.chat.dto.GetGroupMemberResp;
import com.ccj.campus.chat.dto.MapResultHandler;
import com.ccj.campus.chat.entity.GroupMember;
import com.ccj.campus.chat.enums.GroupRoleTypeEnum;
import com.ccj.campus.chat.mapper.GroupMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 群成员表 服务实现类
 * </p>
 *
 * @author CCJ
 * @since 2024-05-06
 */
@Service
@RequiredArgsConstructor
public class GroupMemberDao extends ServiceImpl<GroupMemberMapper, GroupMember> {

    private final GroupMemberMapper groupMemberMapper;

    public GroupMember getMember(Long uid, Long groupId) {
        return lambdaQuery().eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUid, uid)
                .one();
    }

    public List<Long> getMembersUid(Long groupId) {
        return groupMemberMapper.getMembersUid(groupId);
    }


    /**
     * 单个群聊的管理员和群主
     */
    public List<Long> getManagersOrOwner(Long groupId) {
        return groupMemberMapper.getManagersOrOwner(groupId, GroupRoleTypeEnum.GROUP_OWNER.getType(), GroupRoleTypeEnum.GROUP_MANAGER.getType());
    }


    public Map<Long, Integer> getRoomMemberById(List<Long> needLoadRoomId) {
        MapResultHandler<Long, Integer> resultHandler = new MapResultHandler<>();
        if (needLoadRoomId != null && !needLoadRoomId.isEmpty()) {
            groupMemberMapper.getRoomMemberById(needLoadRoomId, resultHandler);
        }
        return resultHandler.getMappedResults();
    }

    public GroupMember getRole(Long uid, Long groupId) {
        return lambdaQuery().eq(GroupMember::getUid, uid)
                .eq(GroupMember::getGroupId, groupId)
                .one();
    }

    public List<GetGroupMemberResp> getRoomMembersInfo(Long groupId, GetGroupMemberReq req) {
        return groupMemberMapper.getRoomMembersInfo(groupId, req.getPageSize(), req.getCursor());
    }

    public GetGroupMemberResp getRoomMembersInfoOne(Long groupId, String cursor) {
        return groupMemberMapper.getRoomMembersInfoOne(groupId, cursor);
    }

    public Integer countMyGroup(Long uid) {
        return lambdaQuery().eq(GroupMember::getUid, uid)
                .eq(GroupMember::getRole, GroupRoleTypeEnum.GROUP_OWNER.getType())
                .count();
    }

    public void transferOwner(Long groupId, Long uid, Long targetUid) {
        lambdaUpdate().eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUid, targetUid)
                .set(GroupMember::getRole, GroupRoleTypeEnum.GROUP_OWNER.getType())
                .update();

        lambdaUpdate().eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUid, uid)
                .set(GroupMember::getRole, GroupRoleTypeEnum.GROUP_MEMBER.getType())
                .update();
    }

    public List<Long> getUserAllGroup(Long uid) {
        return groupMemberMapper.getUserAllGroup(uid);
    }

    public IPage<GroupMember> getGroupMemberListPage(Long groupId, GetGroupMemberBaseReq req) {
        return lambdaQuery().eq(GroupMember::getGroupId, groupId)
                .select(GroupMember::getUid, GroupMember::getRole)
                .page(req.plusPage());
    }
}
