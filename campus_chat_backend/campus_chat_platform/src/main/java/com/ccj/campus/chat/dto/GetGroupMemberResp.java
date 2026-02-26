package com.ccj.campus.chat.dto;

import lombok.Data;

/**
 * @Author ccj
 * @Date 2024-06-30 19:29
 * @Description
 */
@Data
public class GetGroupMemberResp {

    /**
     * 游标字段
     */
    private Long id;


    private Long uid;

    /**
     * 头像
     */
    private String avatar;

    private String fullName;
}
