package com.ccj.campus.chat.controller;

import com.ccj.campus.chat.cache.GroupMemberCache;
import com.ccj.campus.chat.cache.RoomCache;
import com.ccj.campus.chat.cache.RoomFriendCache;
import com.ccj.campus.chat.dto.ApiResult;
import com.ccj.campus.chat.dto.CursorPageBaseResp;
import com.ccj.campus.chat.entity.Room;
import com.ccj.campus.chat.entity.RoomFriend;
import com.ccj.campus.chat.enums.RoomTypeEnum;
import com.ccj.campus.chat.imservice.domain.vo.req.ChatMessagePageReq;
import com.ccj.campus.chat.imservice.domain.vo.req.ChatMessageReq;
import com.ccj.campus.chat.imservice.domain.vo.req.RecallMessageReq;
import com.ccj.campus.chat.imservice.domain.vo.req.ReadContactMsgReq;
import com.ccj.campus.chat.imservice.domain.vo.resp.ChatContactResp;
import com.ccj.campus.chat.imservice.domain.vo.resp.ChatMessageResp;
import com.ccj.campus.chat.imservice.domain.vo.resp.WSBaseResponse;
import com.ccj.campus.chat.imservice.service.WebSocketService;
import com.ccj.campus.chat.imservice.service.adapter.WebSocketAdapter;
import com.ccj.campus.chat.service.ChatService;
import com.ccj.campus.chat.util.RequestHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private RoomCache roomCache;

    @Autowired
    private RoomFriendCache roomFriendCache;

    @Autowired
    private GroupMemberCache groupMemberCache;

    @PostMapping("/message/send")
    public ApiResult<ChatMessageResp> sendMessage(@RequestBody @Valid ChatMessageReq req) {
        Long uid = RequestHolder.get().getUid();
        Long messageId = chatService.sendMessage(uid, req, true);
        ChatMessageResp resp = chatService.getMessageResp(messageId, uid);

        WSBaseResponse<ChatMessageResp> wsResp = WebSocketAdapter.buildChatMessageResp(resp);
        resolveRoomMemberUids(req.getRoomId()).forEach(targetUid -> webSocketService.sendToOnlineUser(targetUid, wsResp));

        return ApiResult.success(resp);
    }

    @PostMapping("/message/page")
    public ApiResult<CursorPageBaseResp<ChatMessageResp>> getMessagePage(@RequestBody @Valid ChatMessagePageReq req) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(chatService.getMessageList(uid, req));
    }

    @PostMapping("/message/recall")
    public ApiResult<Void> recallMessage(@RequestBody @Valid RecallMessageReq req) {
        Long uid = RequestHolder.get().getUid();
        chatService.recallMessage(uid, req);
        return ApiResult.success();
    }

    @PostMapping("/message/delete")
    public ApiResult<Void> deleteMessage(@RequestBody @Valid RecallMessageReq req) {
        Long uid = RequestHolder.get().getUid();
        chatService.deleteMessage(uid, req);
        return ApiResult.success();
    }

    @PostMapping("/contact/recent")
    public ApiResult<List<ChatContactResp>> getRecentContactList() {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(chatService.getRecentContactList(uid));
    }

    @PostMapping("/contact/read")
    public ApiResult<Void> readContactMsg(@RequestBody @Valid ReadContactMsgReq req) {
        Long uid = RequestHolder.get().getUid();
        chatService.readMsg(uid, req);
        return ApiResult.success();
    }

    private Set<Long> resolveRoomMemberUids(Long roomId) {
        Set<Long> uidSet = new HashSet<>();
        Room room = roomCache.get(roomId);
        if (room == null) {
            return uidSet;
        }

        if (RoomTypeEnum.isGroupRoom(room.getType())) {
            uidSet.addAll(groupMemberCache.getGroupMember(roomId));
            return uidSet;
        }

        RoomFriend roomFriend = roomFriendCache.get(roomId);
        if (roomFriend == null) {
            return uidSet;
        }

        if (roomFriend.getUid1() != null) uidSet.add(roomFriend.getUid1());
        if (roomFriend.getUid2() != null) uidSet.add(roomFriend.getUid2());
        return uidSet;
    }
}
