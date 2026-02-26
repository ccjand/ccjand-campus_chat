package com.ccj.campus.chat.imservice.domain.vo.resp;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatContactResp {

    private Long id;

    private Long roomId;

    private String messageType;

    private String name;

    private String avatar;

    private String summary;

    private Integer unreadCount;

    private LocalDateTime timestamp;

    private Long peerUid;
}

