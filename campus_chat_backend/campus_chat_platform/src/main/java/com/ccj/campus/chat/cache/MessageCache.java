package com.ccj.campus.chat.cache;

import com.ccj.campus.chat.entity.Messages;
import com.ccj.campus.chat.mapper.MessagesMapper;
import com.ccj.campus.chat.util.JsonUtils;
import com.ccj.campus.chat.utils.RedisKey;
import com.ccj.campus.chat.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Author ccj
 * @Date 2024-05-14 10:57
 * @Description 短暂存储信息, 上下游避免重复组装消息
 */
@Component
@RequiredArgsConstructor
public class MessageCache {

    private final MessagesMapper messagesMapper;

    public static String getKey(Long messageId) {
        return RedisKey.getKey(RedisKey.MESSAGE_KEY, messageId);
    }

    public void setTemporaryMessage(Messages message) {
        String key = getKey(message.getId());
        //缓存消息3分钟, 方便在2分钟内撤回消息的时候也可以走缓存
        RedisUtils.set(key, message, RedisKey.MESSAGE_EXPIRE_TIME, TimeUnit.MINUTES);
    }


    public void setTemporaryMessage(Messages message, long time, TimeUnit timeUnit) {
        String key = getKey(message.getId());
        //缓存消息2分钟, 方便在2分钟内撤回消息的时候也可以走缓存
        RedisUtils.set(key, message, time, timeUnit);
    }

    public void deleteCache(String key) {
        RedisUtils.del(key);
    }

    public void deleteCache(Long messageId) {
        String key = getKey(messageId);
        RedisUtils.del(key);
    }

    public Messages getAndDeleteMessage(Long messageId) {
        String key = getKey(messageId);
        Messages message = RedisUtils.get(key, Messages.class);
        if (message == null) {
            message = messagesMapper.getByMsgId(messageId);
        } else {
            //缓存的这条消息可以删除了,下游用不到了
            deleteCache(key);
        }
        return message;
    }


    public Messages getMessage(Long messageId) {
        String key = getKey(messageId);
        Messages message = RedisUtils.get(key, Messages.class);
        if (message == null) {
            message = messagesMapper.getByMsgId(messageId);
        }
        return message;
    }

    /**
     * 查询缓存有没有相同的数据, 如果有, 说明重复发送了【消息只保存1分钟，就算数据库有，那1分钟之外的重发可以认为不是同一条消息】
     *
     * @param roomId    房间号
     * @param timestamp 客户端生成的本地时间戳
     * @param msgSeq    客户端生成的本地序列号
     * @param random    客户端生成的随机数
     * @return 返回消息的id
     */
    public Long getRepeatMessage(Long roomId, Long timestamp, Long msgSeq, Integer random) {
        String key = getRepeatMsgKey(roomId, timestamp, msgSeq, random);
        String msgId = RedisUtils.get(key);
        if (msgId != null) {
            return JsonUtils.toObj(msgId, Long.class);
        }
        return null;
    }


    public void saveRepeatMessage(Long roomId, Long timestamp, Long msgSeq, Integer random, Long messageId) {
        String key = getRepeatMsgKey(roomId, timestamp, msgSeq, random);
        RedisUtils.set(key, messageId.toString(), RedisKey.TEMP_REPEAT_MESSAGE_TIME, TimeUnit.MINUTES);
    }


    public String getRepeatMsgKey(Long roomId, Long timestamp, Long msgSeq, Integer random) {
        return RedisKey.getKey(RedisKey.TEMP_SINGLE_REPEAT_MESSAGE, roomId, timestamp, msgSeq, random);
    }

}
