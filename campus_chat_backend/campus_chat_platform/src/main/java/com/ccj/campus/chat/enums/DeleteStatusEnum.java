package com.ccj.campus.chat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author ccj
 * @Date 2024-05-05 15:23
 * @Description
 */
@AllArgsConstructor
@Getter
public enum DeleteStatusEnum {

    /**
     * 已删除
     */
    DELETED(1),

    /**
     * 未删除
     */
    NOT_DELETED(0);

    private Integer status;
}
