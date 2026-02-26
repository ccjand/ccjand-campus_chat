package com.ccj.campus.chat.imservice.service.adapter;

import cn.hutool.core.lang.UUID;
import com.ccj.campus.chat.entity.Users;
import com.ccj.campus.chat.imservice.domain.dto.PullMessagePage;
import com.ccj.campus.chat.imservice.domain.vo.req.FinishCallReq;
import com.ccj.campus.chat.imservice.domain.vo.resp.*;
import com.ccj.campus.chat.imservice.enums.CallStatusEnum;
import com.ccj.campus.chat.imservice.enums.WSResponseTypeEnum;

/**
 * @Author ccj
 * @Date 2024-05-03 17:41
 * @Description
 */
public class WebSocketAdapter {


    public static WSBaseResponse<WSLoginSuccess> buildLoginSuccess(String token, Users user) {
        WSBaseResponse<WSLoginSuccess> resp = new WSBaseResponse<>();
        resp.setType(WSResponseTypeEnum.LOGIN_SUCCESS.getType());
        WSLoginSuccess wsLoginSuccess = WSLoginSuccess.builder()
                .uid(user.getId())
                .avatar(user.getAvatar())
                .fullName(user.getFullName())
                .token(token)
                .build();
        resp.setData(wsLoginSuccess);

        return resp;
    }

    public static WSBaseResponse<WSFriendApplication> buildFriendApplyResp(WSFriendApplication application) {
        WSBaseResponse<WSFriendApplication> resp = new WSBaseResponse<>();
        resp.setType(WSResponseTypeEnum.APPLY.getType());
        resp.setData(application);
        return resp;
    }

    public static WSBaseResponse<ChatMessageResp> buildChatMessageResp(ChatMessageResp resp) {
        WSBaseResponse<ChatMessageResp> baseResponse = new WSBaseResponse<>();
        baseResponse.setType(WSResponseTypeEnum.PUSH_MODE_MESSAGE.getType());
        baseResponse.setData(resp);
        return baseResponse;
    }

    public static WSBaseResponse<WSMsgRecall> buildRecallResp(WSMsgRecall recall) {
        WSBaseResponse<WSMsgRecall> resp = new WSBaseResponse<>();
        resp.setType(WSResponseTypeEnum.RECALL.getType());
        resp.setData(recall);
        return resp;
    }

    public static WSBaseResponse<Integer> buildErrorResp(int notFriend) {
        WSBaseResponse<Integer> response = new WSBaseResponse<>();
        response.setType(WSResponseTypeEnum.ERROR.getType());
        response.setData(notFriend);
        return response;
    }


    public static WSBaseResponse<FinishCallResp> buildFinishCallResp(FinishCallReq finishCallReq) {
        WSBaseResponse<FinishCallResp> resp = new WSBaseResponse<>();
        FinishCallResp finishCallResp = new FinishCallResp();
        finishCallResp.setCalleeUId(finishCallReq.getCalleeUId());
        finishCallResp.setCallerUId(finishCallReq.getCallerUId());
        finishCallResp.setMessageId(finishCallReq.getMessageId());
        finishCallResp.setRoomId(finishCallReq.getRoomId());

        resp.setType(CallStatusEnum.finish.getType());
        resp.setData(finishCallResp);
        return resp;
    }

    public static WSBaseResponse<String> buildAuthErrorResp(String errMsg) {
        WSBaseResponse<String> resp = new WSBaseResponse<>();
        resp.setData(errMsg);
        resp.setType(WSResponseTypeEnum.UNAUTHORIZED.getType());
        return resp;
    }

    public static WSBaseResponse<PullMessagePage> buildPullMessageResp(PullMessagePage pullMessagePage, WSResponseTypeEnum typeEnum) {
        WSBaseResponse<PullMessagePage> resp = new WSBaseResponse<>();
        resp.setType(typeEnum.getType());
        resp.setData(pullMessagePage);
        return resp;
    }

    public static WSBaseResponse<PullMessagePage> buildPullEmptyMessageResp(WSResponseTypeEnum typeEnum) {
        WSBaseResponse<PullMessagePage> resp = new WSBaseResponse<>();
        resp.setType(typeEnum.getType());
        resp.setData(PullMessagePage.empty());
        return resp;
    }
}
