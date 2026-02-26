package com.ccj.campus.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ccj.campus.chat.dto.MapResultHandler;
import com.ccj.campus.chat.entity.Messages;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface MessagesMapper extends BaseMapper<Messages> {
    void getMsgReadCount(@Param("roomId") Long roomId, @Param("uid") Long uid, @Param("messageIdList") List<Long> messageIdList, MapResultHandler<Long, Integer> resultHandler);

    List<Messages> pullMessageList(@Param("roomIdLastMsgIdMap") Map<Long, Long> contactMessageMap, @Param("batchSize") Integer batchSize);

    List<Messages> pullIntervalMessageList(@Param("roomId") Long roomId, @Param("lastPullMessageId") Long lastPullMessageId, @Param("lastPushMessageId") Long lastPushMessageId, @Param("batchSize") Integer batchSize);

    Messages getByMsgId(@Param("id") Long id);

    int updateMessageFieldsById(@Param("id") Long id,
                                @Param("content") String content,
                                @Param("replyMsgId") Long replyMsgId,
                                @Param("gapCount") Integer gapCount,
                                @Param("type") Integer type,
                                @Param("status") Integer status,
                                @Param("extraJson") String extraJson);
}
