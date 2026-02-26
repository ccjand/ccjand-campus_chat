package com.ccj.campus.chat.imservice.domain.dto;

import com.ccj.campus.chat.imservice.domain.vo.resp.WSBaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @Author ccj
 * @Date 2024-05-13 15:52
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushMessageDTO {

    /**
     * 推送的ws消息
     */
    private WSBaseResponse<?> wsBaseMsg;

    /**
     * 推送的uid
     */
    private Set<Long> uidList;

    /**
     * 推送类型 1个人 2全员
     *
     * @see com.ccj.campus.chat.imservice.enums.WSPushTypeEnum
     */
    private Integer pushType;
    
}
