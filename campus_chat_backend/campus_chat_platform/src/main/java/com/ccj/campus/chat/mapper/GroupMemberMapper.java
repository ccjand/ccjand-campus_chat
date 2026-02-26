package com.ccj.campus.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ccj.campus.chat.dto.GetGroupMemberResp;
import com.ccj.campus.chat.dto.MapResultHandler;
import com.ccj.campus.chat.entity.GroupMember;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 群成员表 Mapper 接口
 * </p>
 *
 * @author CCJ
 * @since 2024-05-06
 */
public interface GroupMemberMapper extends BaseMapper<GroupMember> {

    /**
     * 查询某个群所有管理员和群主的uid
     */
    List<Long> getManagersOrOwner(@Param("groupId") Long groupId, @Param("owner") Integer owner, @Param("manager") Integer manager);

    /**
     * 获取群房间的人员数
     */
    void getRoomMemberById(@Param("needLoadRoomId") List<Long> needLoadRoomId, MapResultHandler<Long, Integer> mapResultHandler);

    List<GetGroupMemberResp> getRoomMembersInfo(@Param("groupId") Long groupId, @Param("pageSize") Integer pageSize, @Param("cursor") String cursor);

    GetGroupMemberResp getRoomMembersInfoOne(@Param("groupId") Long groupId, @Param("cursor") String cursor);

    List<Long> getUserAllGroup(@Param("uid") Long uid);

    List<Long> getMembersUid(@Param("groupId") Long groupId);
}
