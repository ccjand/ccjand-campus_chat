package com.ccj.campus.chat.secureinvoke.dao;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ccj.campus.chat.secureinvoke.entity.SecureInvokeRecord;
import com.ccj.campus.chat.secureinvoke.mapper.SecureInvokeRecordMapper;
import com.ccj.campus.chat.secureinvoke.service.SecureInvokeService;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @Author ccj
 * @Date 2024-05-14 20:08
 * @Description
 */
@Component
public class SecureInvokeRecordDao extends ServiceImpl<SecureInvokeRecordMapper, SecureInvokeRecord> {

    public List<SecureInvokeRecord> getWaitRetryRecord() {
        Date now = new Date();
        //查n分钟前的数据,避免数据刚刚入库没查到
        DateTime afterTime = DateUtil.offsetMinute(now, SecureInvokeService.RETRY_INTERVAL_MINUTE);
        return lambdaQuery().eq(SecureInvokeRecord::getStatus, SecureInvokeRecord.STATUE_WAIT)
                .lt(SecureInvokeRecord::getNextRetryTime, now)
                .lt(SecureInvokeRecord::getCreateTime, afterTime)
                .list();
    }
}
