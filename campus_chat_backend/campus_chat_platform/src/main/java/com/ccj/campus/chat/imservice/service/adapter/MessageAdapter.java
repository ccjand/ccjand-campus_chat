package com.ccj.campus.chat.imservice.service.adapter;

import com.ccj.campus.chat.entity.MessageExtra;
import com.ccj.campus.chat.entity.Messages;
import com.ccj.campus.chat.entity.Users;
import com.ccj.campus.chat.enums.DeleteStatusEnum;
import com.ccj.campus.chat.enums.MessageTypeEnum;
import com.ccj.campus.chat.imservice.domain.vo.req.ChatMessageReq;
import com.ccj.campus.chat.imservice.domain.vo.resp.ChatMessageResp;
import com.ccj.campus.chat.imservice.service.MessageHandlerFactory;
import com.ccj.campus.chat.strategy.AbstractMessageHandler;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-05-07 17:06
 * @Description
 */
public class MessageAdapter {


    /**
     * 距离你回复的消息, 最多只能间隔100条
     */
    public static final Integer MAX_REPLY_MESSAGE_GAP_COUNT = 100;

    public static void main(String[] args) {
        System.out.println(LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")));
        System.out.println(System.currentTimeMillis());
    }

    public static Messages buildMessageSave(Long uid, ChatMessageReq chatMessageReq) {


        Long fromUid = MessageTypeEnum.isSystemMessage(chatMessageReq.getMsgType()) ? Users.SYSTEM_ID : uid;
        String clientMsgId = chatMessageReq.getClientMsgId();
        MessageExtra extra = null;
        if (clientMsgId != null && !clientMsgId.isBlank()) {
            extra = new MessageExtra();
            extra.setClientMsgId(clientMsgId);
        }
        return Messages.builder()
                .roomId(chatMessageReq.getRoomId())
                .fromUid(fromUid)
                .type(chatMessageReq.getMsgType())
                .status(DeleteStatusEnum.NOT_DELETED.getStatus())
                .extra(extra)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    public static List<ChatMessageResp> buildChatMesasgeRespList(List<Messages> messageList, Long receiveUid) {


        return messageList.stream().filter(Objects::nonNull).parallel().map(message -> {
                    ChatMessageResp resp = new ChatMessageResp();
                    resp.setFromUser(buildFromUserInfo(message.getFromUid()));
                    ChatMessageResp.Message msg = buildMessageResp(message, receiveUid);
                    msg.setMsgSeq(message.getMsgSeq());
                    resp.setMessage(msg);
                    return resp;
                })
                .sorted(Comparator.comparing(resp -> resp.getMessage().getSendTime())) //根据发送时间排好序,方便前端展示
                .collect(Collectors.toList());
    }

    private static ChatMessageResp.Message buildMessageResp(Messages message, Long receiveUid) {
        AbstractMessageHandler<?> handler = MessageHandlerFactory.getMessageHandlerNonNull(message.getType());
        Object messageContent = handler.showMsg(message);

        ChatMessageResp.Message msg = new ChatMessageResp.Message();
        msg.setId(message.getId());
        msg.setRoomId(message.getRoomId());
        msg.setSendTime(message.getCreateTime() != null ? message.getCreateTime() : LocalDateTime.now());
        msg.setType(message.getType());
        msg.setClientMsgId(Optional.ofNullable(message.getExtra()).map(MessageExtra::getClientMsgId).orElse(null));
        msg.setMessageContent(messageContent);
        return msg;
    }

    private static ChatMessageResp.UserInfo buildFromUserInfo(Long fromUid) {
        ChatMessageResp.UserInfo userInfo = new ChatMessageResp.UserInfo();
        userInfo.setUid(fromUid);
        return userInfo;
    }
}
