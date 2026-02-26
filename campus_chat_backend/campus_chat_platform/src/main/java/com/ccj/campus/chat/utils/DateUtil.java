package com.ccj.campus.chat.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @Author ccj
 * @Date 2024-07-02 12:28
 * @Description
 */
public class DateUtil {

    /**
     * 时间戳转日期字符串
     */
    public static String timeStampToDate(Long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");
        return localDateTime.format(formatter);
    }

    public static long getTimestampInSecond() {
        return LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
    }

}
