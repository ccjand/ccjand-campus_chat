package com.ccj.campus.chat.imservice.domain.dto;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author ccj
 * @Date 2024-05-14 12:39
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WSTerminalChanelExtraDTO {
    private Channel channel;

    /**
     * @see com.ccj.campus.chat.imservice.enums.TerminalTypeEnum
     */
    private Integer terminalType;
}
