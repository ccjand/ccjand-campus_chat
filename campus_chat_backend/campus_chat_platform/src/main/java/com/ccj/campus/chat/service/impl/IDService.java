package com.ccj.campus.chat.service.impl;

import cn.hutool.http.HttpUtil;
import com.ccj.campus.chat.dto.Result;
import com.ccj.campus.chat.dto.Status;
import com.ccj.campus.chat.util.JsonUtils;
import com.ccj.campus.chat.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @Author ccj
 * @Date 2024-05-06 17:42
 * @Description 调用全局id生成服务
 */
@Service
public class IDService {

    @Value("${leaf.url.roomMsgSeq}")
    private String LEAF_URL_ROOM_MSG_SEQ;

    @Value("${leaf.url.msgId}")
    private String LEAF_URL_MSG_ID;


    /**
     * 获取房间内唯一递增id
     */
    public Long getRoomMsgSeq(Long roomId) {
        String respJson = HttpUtil.get(LEAF_URL_ROOM_MSG_SEQ + "/" + roomId);
        AssertUtil.isNotEmpty(respJson, "获取房间内唯一递增id失败");
        Result result = JsonUtils.toObj(respJson, Result.class);
        AssertUtil.isTrue(Status.SUCCESS.equals(result.getStatus()), "获取房间内唯一递增id失败");
        return result.getId();
    }

    /**
     * 获取全局递增的消息id
     */
    public Long getMessageId() {
        try {
            String respJson = HttpUtil.get(LEAF_URL_MSG_ID);
            AssertUtil.isNotEmpty(respJson, "获取全局递增的消息id失败");
            Result result = JsonUtils.toObj(respJson, Result.class);
            AssertUtil.isTrue(Status.SUCCESS.equals(result.getStatus()), "获取全局递增的消息id失败");
            return result.getId();
        } catch (Exception e) {
            return null;
        }
    }
}
