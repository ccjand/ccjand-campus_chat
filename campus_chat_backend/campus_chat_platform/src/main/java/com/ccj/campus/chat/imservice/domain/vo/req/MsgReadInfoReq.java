package com.ccj.campus.chat.imservice.domain.vo.req;

import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * @Author ccj
 * @Date 2024-06-30 12:03
 * @Description
 */
@Data
public class MsgReadInfoReq {

    @Size(min = 1, max = 20, message = "查看最多20条消息")
    private List<Long> msgIds;
}
