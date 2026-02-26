package com.ccj.campus.chat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * @Author ccj
 * @Date 2024-05-10 11:53
 * @Description 群员角色
 */
@AllArgsConstructor
@Getter
public enum GroupRoleTypeEnum {

    GROUP_OWNER(1),//群主
    GROUP_MANAGER(2),//管理员
    GROUP_MEMBER(3),//普通成员
    KICK_OUT(4); //被踢出群聊的人

    private final Integer type;


    public static boolean isManagerOrOwner(Integer roleType) {
        return GROUP_OWNER.getType().equals(roleType) || GROUP_MANAGER.getType().equals(roleType);
    }

    public static boolean isOwner(Integer roleType) {
        return GROUP_OWNER.getType().equals(roleType);
    }
}
