package com.ccj.campus.chat.imservice.domain.vo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author ccj
 * @Date 2024-05-04 12:28
 * @Description websocket的请求基类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WSBaseRequest {
    private Integer type;
    private String data;
}
