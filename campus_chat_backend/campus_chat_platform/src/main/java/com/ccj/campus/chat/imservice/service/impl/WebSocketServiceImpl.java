package com.ccj.campus.chat.imservice.service.impl;

import cn.hutool.core.util.StrUtil;

import com.ccj.campus.chat.cache.UserInfoCache;
import com.ccj.campus.chat.config.ThreadPoolConfig;
import com.ccj.campus.chat.dto.UserOfflineEvent;
import com.ccj.campus.chat.dto.UserOnlineEvent;
import com.ccj.campus.chat.entity.Users;
import com.ccj.campus.chat.imservice.domain.dto.PullMessagePage;
import com.ccj.campus.chat.imservice.domain.dto.WSChannelExtraDTO;
import com.ccj.campus.chat.imservice.domain.dto.WSTerminalChanelExtraDTO;
import com.ccj.campus.chat.imservice.domain.vo.req.PullIntervalMessageReq;
import com.ccj.campus.chat.imservice.domain.vo.req.PullMessageListReq;
import com.ccj.campus.chat.imservice.domain.vo.resp.WSBaseResponse;
import com.ccj.campus.chat.imservice.enums.UserActiveStatusEnum;
import com.ccj.campus.chat.imservice.enums.WSResponseTypeEnum;
import com.ccj.campus.chat.imservice.service.WebSocketService;
import com.ccj.campus.chat.imservice.service.adapter.WebSocketAdapter;
import com.ccj.campus.chat.service.ChatService;
import com.ccj.campus.chat.util.JsonUtils;
import com.ccj.campus.chat.utils.JwtUtils;
import com.ccj.campus.chat.utils.NettyUtils;
import com.ccj.campus.chat.utils.RedisKey;
import com.ccj.campus.chat.utils.RedisUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author ccj
 * @Date 2024-05-03 15:12
 * @Description
 */
@Service
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private UserInfoCache userInfoCache;

    @Autowired
    @Qualifier(value = ThreadPoolConfig.WS_EXECUTOR)
    private ThreadPoolTaskExecutor executor;

    @Autowired
    @Lazy
    private ChatService chatService;

    /**
     * 管道和管道附加信息
     * key: channel
     * value: 用户基本信息
     */
    private final static Map<Channel, WSChannelExtraDTO> ONLINE_CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * 用户uid和管道 【多端登录】， 不用担心CopyOnWriteArrayList内存问题,用户的channel顶多就app、桌面、web端等少量方式
     * key: 用户uid
     * value: 用户的多个终端链接
     */
    private final static Map<Long, CopyOnWriteArrayList<WSTerminalChanelExtraDTO>> ONLINE_UID_MAP = new ConcurrentHashMap<>();

    @Override
    public void connect(Channel channel) {
        //这里还拿不到uid和terminalType, 但是在下方的 online()方法有配置
        WSChannelExtraDTO channelExtraDTO = new WSChannelExtraDTO(null, null);
        ONLINE_CHANNEL_MAP.putIfAbsent(channel, channelExtraDTO);
        System.out.println("全部在线用户:" + ONLINE_CHANNEL_MAP.size());
    }


    @Override
    public void remove(Channel channel) {
        WSChannelExtraDTO extraDTO = ONLINE_CHANNEL_MAP.get(channel);
        Long uid = Optional.ofNullable(extraDTO).map(WSChannelExtraDTO::getUid).orElse(null);
        Integer terminalType = Optional.ofNullable(extraDTO).map(WSChannelExtraDTO::getTerminalType).orElse(null);

        //下线用户的某个终端
        boolean successful = offlineTerminal(channel, uid, terminalType);

        //下线成功
        if (uid != null && successful) {
            Users update = new Users();
            update.setId(uid);
            update.setActiveStatus(UserActiveStatusEnum.OFFLINE.getCode());
            update.setLastOptTime(LocalDateTime.now());
            applicationEventPublisher.publishEvent(new UserOfflineEvent(this, update));
            //用户端已经断开连接了就收不到消息, 不需要让别人知道, 只需要修改redis中的状态, 其他的按照客户端的定时拉取
            sendOfflineMessage(channel);
        }

    }

    /**
     * 用户下线
     *
     * @return 是否下线成功
     */

    private boolean offlineTerminal(Channel channel, Long uid, Integer terminalType) {
        ONLINE_CHANNEL_MAP.remove(channel);
        //还需要下线具体的终端, 因为用户可能有很多终端在线
        if (uid != null && terminalType != null) {
            CopyOnWriteArrayList<WSTerminalChanelExtraDTO> multiChannel = ONLINE_UID_MAP.get(uid);
            //ch.getChannel().close();
            //他也保存在ONLINE_CHANNEL_MAP中， 在ONLINE_CHANNEL_MAP中 close就行了
            multiChannel.removeIf(ch -> ch.getTerminalType().equals(terminalType));

            Channel terminalChannel = multiChannel.stream().filter(ch -> ch.getTerminalType().equals(terminalType)).findFirst().map(WSTerminalChanelExtraDTO::getChannel).orElse(null);

            return ONLINE_CHANNEL_MAP.get(channel) == null && terminalChannel == null;
        }

        System.out.println("有人下线了目前在线用户:" + ONLINE_CHANNEL_MAP.size());

        return true;
    }

    @Override
    public void authorize(Channel channel, String token) {
        Long uid = getValidUid(channel, token);
        if (uid != null) {
            //token 有效， 返回用户信息
            Users user = userInfoCache.get(uid);
            loginSuccessful(channel, user, token);
        } else {
            //没有登录，断开他的连接
            remove(channel);
            sendMsg(channel, WebSocketAdapter.buildAuthErrorResp("请先登录"));
            channel.close();
        }
    }


    @Override
    public void sendToOnlineUser(Long uid, WSBaseResponse<?> wsBaseMsg) {
        CopyOnWriteArrayList<WSTerminalChanelExtraDTO> channels = ONLINE_UID_MAP.get(uid);
        if (channels == null || channels.isEmpty()) {
            log.info("用户{} 所有端均 离线, 在线推送消息给用户失败", uid);
            return;
        }

        //推送给在线用户的全部端， 除了发送者的那一终端类型
        channels.forEach(terminalChannel -> executor.execute(
                () -> sendMsg(terminalChannel.getChannel(), wsBaseMsg)));
    }

    @Override
    public void sendToOnlineUser(Long uid, WSBaseResponse<?> wsBaseMsg, Long skipUid) {
        CopyOnWriteArrayList<WSTerminalChanelExtraDTO> channels = ONLINE_UID_MAP.get(uid);
        if (channels == null || channels.isEmpty()) {
            log.info("用户{} 所有端均 离线, 在线推送消息给用户失败", uid);
            return;
        }

        //推送给在线用户的全部端， 除了发送者的那一终端类型
        channels.forEach(terminalChannel -> {
            if (!NettyUtils.getAttr(terminalChannel.getChannel(), NettyUtils.UID).equals(skipUid)) {
                executor.execute(() -> sendMsg(terminalChannel.getChannel(), wsBaseMsg));
            }
        });
    }

    private void loginSuccessful(Channel channel, Users user, String token) {
        //更新上线列表
        online(channel, user.getId());

        //推送用户登录成功的消息【包装返回用户基本信息】
        sendMsg(channel, WebSocketAdapter.buildLoginSuccess(token, user));

        //发布用户上线的事件
        user.setLastOptTime(LocalDateTime.now());

        applicationEventPublisher.publishEvent(new UserOnlineEvent(this, user));
    }


    /**
     * 用户上线
     */
    private static void online(Channel channel, Long uid) {
        //保存用户连接和用户 uid 的关系
        WSChannelExtraDTO channelExtraDTO = ONLINE_CHANNEL_MAP.get(channel);
        Integer terminalType = NettyUtils.getAttr(channel, NettyUtils.TERMINAL_TYPE);
        channelExtraDTO.setUid(uid);
        channelExtraDTO.setTerminalType(terminalType);//终端类型

        //保存用户的所有终端连接【uid和用户所有的channel映射】
        CopyOnWriteArrayList<WSTerminalChanelExtraDTO> terminals = ONLINE_UID_MAP.getOrDefault(uid, new CopyOnWriteArrayList<>());

        //避免同一个终端类型有多个Chanel的情况, 这样会导致发送消息失败， 其他用户会收到影响
        // 查找当前终端类型的旧连接
        List<WSTerminalChanelExtraDTO> oldChannels = terminals.stream()
                .filter(terminal -> terminal.getTerminalType().equals(terminalType))
                .toList();

        // 如果存在旧连接，则关闭并移除
        if (!oldChannels.isEmpty()) {
            for (WSTerminalChanelExtraDTO old : oldChannels) {
                try {
                    // 1. 关闭旧的物理连接
                    if (old.getChannel().isActive()) {
                        old.getChannel().close();
                    }
                    // 2. 清理旧连接的映射关系
                    ONLINE_CHANNEL_MAP.remove(old.getChannel());
                    // 3. 从列表中移除
                    terminals.remove(old);
                } catch (Exception e) {
                    log.error("关闭旧连接失败: uid={}, terminalType={}", uid, terminalType, e);
                }
            }
        }
        
        // 添加新连接
        terminals.add(new WSTerminalChanelExtraDTO(channel, terminalType));

        ONLINE_UID_MAP.put(uid, terminals);

        NettyUtils.setAttr(channel, NettyUtils.UID, uid);//后续使用
    }

    public Long getValidUid(Channel channel, String token) {
        Long uid = jwtUtils.getUidOrNull(token);

        if (uid == null) {
            return null;
        }

        //拼接得到redis 的 key
        Integer terminalType = NettyUtils.getAttr(channel, NettyUtils.TERMINAL_TYPE);

        String userKey = RedisKey.getKey(RedisKey.USER_TOKEN, uid, terminalType);

        String latestToken = RedisUtils.getStr(userKey);

        //非登录态
        if (StrUtil.isBlank(latestToken)) {
            return null;
        }

        //避免拿着老的 token 还能继续登录
        if (!token.equals(latestToken)) {
            return null;
        }

        return uid;
    }


    /**
     * 向管道发送消息
     */
    @Override
    public void sendMsg(Channel channel, WSBaseResponse<?> resp) {
        channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.toStr(resp)));
    }

    @Override
    public void sendOfflineMessage(Channel channel) {
        //推送离线消息, 客户端收到后, 就会知道需要再次重新连接
        WSBaseResponse<Object> offlineMsg = new WSBaseResponse<>();
        offlineMsg.setType(WSResponseTypeEnum.ONLINE_OFFLINE_NOTIFY.getType());
        offlineMsg.setData(null);
        sendMsg(channel, offlineMsg);
    }

    @Override
    public void requestPullIntervalMessages(Channel channel, PullIntervalMessageReq req) {
        Long uid = NettyUtils.getAttr(channel, NettyUtils.UID);
        PullMessagePage pullMessagePage = chatService.pullIntervalMessageList(uid, req);
        //没有最新消息就不发送
        if (Boolean.TRUE.equals(pullMessagePage.getIsLast())) {
            WSBaseResponse<PullMessagePage> resp = WebSocketAdapter.
                    buildPullEmptyMessageResp(WSResponseTypeEnum.PULL_MODE_INTERVAL_MESSAGE);
            sendMsg(channel, resp);
            return;
        }

        WSBaseResponse<PullMessagePage> resp = WebSocketAdapter.
                buildPullMessageResp(pullMessagePage, WSResponseTypeEnum.PULL_MODE_INTERVAL_MESSAGE);
        //哪个终端请求的就发送给哪个终端
        sendMsg(channel, resp);
    }

    @Override
    public boolean hasChannel(Long uid) {
        CopyOnWriteArrayList<WSTerminalChanelExtraDTO> chanelExtraDTOS = ONLINE_UID_MAP.get(uid);
        return chanelExtraDTOS != null && chanelExtraDTOS.size() > 0;
    }

    @Override
    public void pullRemainMessage(Channel channel, PullMessageListReq req) {
        Long uid = NettyUtils.getAttr(channel, NettyUtils.UID);
        PullMessagePage pullMessagePage = chatService.pullMessageList(uid, req);
        //没有后续消息了
        if (Boolean.TRUE.equals(pullMessagePage.getIsLast())) {
            WSBaseResponse<PullMessagePage> resp = WebSocketAdapter.buildPullEmptyMessageResp(WSResponseTypeEnum.PULL_MODE_MESSAGE);
            sendMsg(channel, resp);
            return;
        }

        WSBaseResponse<PullMessagePage> resp = WebSocketAdapter.buildPullMessageResp(pullMessagePage, WSResponseTypeEnum.PULL_MODE_MESSAGE);
        //哪个终端请求的就发送给哪个终端
        sendMsg(channel, resp);
    }

    @Override
    public void pushOfflineMessage(Channel channel, PullMessageListReq req) {
        Long uid = NettyUtils.getAttr(channel, NettyUtils.UID);
        PullMessagePage pullMessagePage = chatService.pullMessageList(uid, req);
        //没有消息就不发送
        if (Boolean.TRUE.equals(pullMessagePage.getIsLast())) {
            WSBaseResponse<PullMessagePage> resp = WebSocketAdapter.buildPullEmptyMessageResp(WSResponseTypeEnum.OFFLINE_MESSAGE);
            sendMsg(channel, resp);
            return;
        }

        WSBaseResponse<PullMessagePage> resp = WebSocketAdapter.buildPullMessageResp(pullMessagePage, WSResponseTypeEnum.OFFLINE_MESSAGE);
        //哪个终端请求的就发送给哪个终端
        sendMsg(channel, resp);
    }

}
