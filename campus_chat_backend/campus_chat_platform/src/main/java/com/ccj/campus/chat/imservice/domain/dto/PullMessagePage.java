package com.ccj.campus.chat.imservice.domain.dto;

import com.ccj.campus.chat.imservice.domain.vo.resp.ChatMessageResp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * @Author ccj
 * @Date 2024-07-21 01:49
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PullMessagePage {
    private List<ChatMessageResp> pullList;
    private Boolean isLast;

    public static PullMessagePage empty() {
        PullMessagePage pullMessagePage = new PullMessagePage();
        pullMessagePage.setIsLast(true);
        pullMessagePage.setPullList(Collections.emptyList());
        return pullMessagePage;
    }

    public static PullMessagePage init(List<ChatMessageResp> pullList) {
        PullMessagePage pullMessagePage = new PullMessagePage();
        pullMessagePage.setIsLast(false);
        pullMessagePage.setPullList(pullList);
        return pullMessagePage;
    }

}
