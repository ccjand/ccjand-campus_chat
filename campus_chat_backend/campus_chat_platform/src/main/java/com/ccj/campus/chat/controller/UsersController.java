package com.ccj.campus.chat.controller;

import com.ccj.campus.chat.cache.UserInfoCache;
import com.ccj.campus.chat.dto.ApiResult;
import com.ccj.campus.chat.dto.CursorPageBaseReq;
import com.ccj.campus.chat.dto.CursorPageBaseResp;
import com.ccj.campus.chat.dto.UserLoginReq;
import com.ccj.campus.chat.dto.UserLoginResp;
import com.ccj.campus.chat.entity.UserFriend;
import com.ccj.campus.chat.entity.Users;
import com.ccj.campus.chat.service.impl.UserFriendServiceImpl;
import com.ccj.campus.chat.service.UsersService;
import com.ccj.campus.chat.util.RequestHolder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UsersService usersService;

    @Autowired
    private UserFriendServiceImpl userFriendService;

    @Autowired
    private UserInfoCache userInfoCache;

    //登录
    @PostMapping("/login")
    public ApiResult<UserLoginResp> login(@RequestBody UserLoginReq loginReq, HttpServletRequest request) {
        return ApiResult.success(usersService.login(loginReq, request));
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ApiResult<Void> logout() {
        Long uid = RequestHolder.get().getUid();
        usersService.logout(uid);
        return ApiResult.success();
    }

    @PostMapping("/friend/page")
    public ApiResult<CursorPageBaseResp<FriendItem>> friendPage(@RequestBody CursorPageBaseReq req) {
        Long uid = RequestHolder.get().getUid();
        CursorPageBaseResp<UserFriend> page = userFriendService.friendPage(uid, req);
        List<UserFriend> relations = page.getList() == null ? new ArrayList<>() : page.getList();
        List<Long> friendUidList = relations.stream()
                .map(UserFriend::getFriendUid)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, Users> userMap = userInfoCache.getBatch(friendUidList);

        List<FriendItem> items = new ArrayList<>(relations.size());
        for (UserFriend relation : relations) {
            if (relation == null || relation.getFriendUid() == null) {
                continue;
            }
            Users u = userMap.get(relation.getFriendUid());
            if (u == null) {
                continue;
            }
            FriendItem item = new FriendItem();
            item.setUid(u.getId());
            item.setAccountNumber(u.getAccountNumber());
            item.setFullName(u.getFullName());
            item.setAvatar(u.getAvatar());
            item.setRole(u.getRole());
            item.setDepartmentId(u.getDepartmentId());
            item.setClassId(u.getClassId());
            item.setRoomId(relation.getRoomId());
            items.add(item);
        }
        return ApiResult.success(CursorPageBaseResp.init(page, items));
    }

    @Data
    public static class FriendItem {
        private Long uid;
        private String accountNumber;
        private String fullName;
        private String avatar;
        private Integer role;
        private Long departmentId;
        private Long classId;
        private Long roomId;
    }
}
