package com.ccj.campus.chat.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ccj.campus.chat.dto.CursorPageBaseReq;
import com.ccj.campus.chat.dto.CursorPageBaseResp;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @Author ccj
 * @Date 2024-04-18 18:44
 * @Description 游标分页工具类
 */
public class CursorUtils {

    private final static StringRedisTemplate stringRedisTemplate;

    static {
        stringRedisTemplate = SpringUtil.getBean(StringRedisTemplate.class);
    }

    /**
     * 从redis获取游标分页（zset结构）
     * 例如: ZREVRANGEBYSCORE z1 1 -1 WITHSCORES LIMIT 1 3
     * ZREVRANGEBYSCORE key max min WITHSCORES LIMIT offset count
     */
    public static <T> CursorPageBaseResp<T> getCursorPageFromRedisInZSet(String key, CursorPageBaseReq pageBaseReq, Function<String, T> convert) {
        String cursorStr = pageBaseReq.getCursor();
        Set<ZSetOperations.TypedTuple<String>> typedTuples;
        if (cursorStr == null) {
            typedTuples = stringRedisTemplate.opsForZSet()
                    .reverseRangeByScoreWithScores(key, -1, Double.MAX_VALUE, pageBaseReq.getOffset(), pageBaseReq.getPageSize());
        } else {
            double cursor = Double.parseDouble(cursorStr);
            typedTuples = stringRedisTemplate.opsForZSet()
                    .reverseRangeByScoreWithScores(key, -1, cursor, pageBaseReq.getOffset(), pageBaseReq.getPageSize());
        }

        if (typedTuples == null || typedTuples.isEmpty()) {
            return CursorPageBaseResp.empty();
        }


        Double cursor = null;
        int offsetRes = 1;

        List<T> list = new ArrayList<>(typedTuples.size());
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            String setKey = typedTuple.getValue();
            Double score = typedTuple.getScore();
            if (setKey != null && score != null) {
                list.add(convert.apply(setKey));
                if (score.equals(cursor)) {
                    offsetRes++;
                } else {
                    cursor = score;
                    offsetRes = 1;
                }
            }
        }

        Boolean isLast = !pageBaseReq.getPageSize().equals(list.size());
        CursorPageBaseResp<T> page = new CursorPageBaseResp<>();
        page.setCursor(cursor == null ? null : String.valueOf(cursor));
        page.setIsLast(isLast);
        page.setOffset(offsetRes);
        page.setList(list);
        return page;
    }





    public static <T> CursorPageBaseResp<T> getCursorPageFromMysql(IService<T> mapper,
                                                                   CursorPageBaseReq cursorPageBaseReq,
                                                                   Consumer<LambdaQueryWrapper<T>> constraint,
                                                                   SFunction<T, ?> cursorColumn) {
        //游标字段的真实类型
        Class<?> cursorType = LambdaUtils.getReturnType(cursorColumn);
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();

        //游标字段【第一页的游标为 null，后续页的游标为上一页最后一条数据的游标】, 非第一页的就需要拼接上游标字段
        if (!cursorPageBaseReq.isFirstPage()) {
            Object cursor = parseCursor(cursorPageBaseReq.getCursor(), cursorType);
            wrapper.lt(cursorColumn, cursor);
        }
        //游标方向
        wrapper.orderByDesc(cursorColumn);

        //拷贝一份, 后续需要使用, 不然会被以后的污染
        Consumer<LambdaQueryWrapper<T>> constraintCopy = constraint;

        //执行自定义的额外查询条件
        constraint.accept(wrapper);
        //游标翻页结果
        Page<T> page = mapper.page(cursorPageBaseReq.pagePlus(), wrapper);
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            return CursorPageBaseResp.empty();
        }
        //计算游标当前位置
        T lastedElement = CollectionUtils.lastElement(page.getRecords());
        String cursor = Optional.ofNullable(lastedElement)
                .map(cursorColumn)
                .map(CursorUtils::toCursor)
                .orElse(null);

        //是否是最后一页
        Boolean isLast = page.getRecords().size() != cursorPageBaseReq.getPageSize();//如果我要 10 条你只查到了 6 条就是最后一页了
        //多获取一条数据，用于优化判断是否是最后一页【避免总页数是每页请求数的倍数的情况下, 前端需多请求一次才知道是否是最后一页】
        isLast = getNextCursorData(mapper, constraintCopy, cursorColumn, cursorType, cursor, isLast, lastedElement);

        return new CursorPageBaseResp<>(cursor, isLast, page.getRecords());
    }

    private static <T> Boolean getNextCursorData(IService<T> mapper,
                                                 Consumer<LambdaQueryWrapper<T>> constraint,
                                                 SFunction<T, ?> cursorColumn,
                                                 Class<?> cursorType,
                                                 String cursor, Boolean isLast, T lastedElement) {

        if (cursor == null || lastedElement == null) {
            return Boolean.TRUE;
        }

        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        Boolean isDataType = Optional.ofNullable(lastedElement).map(cursorColumn).map(cor -> cor instanceof Date).orElse(false);

        if (isDataType.booleanValue()) {
            wrapper.lt(cursorColumn, new Date(Long.parseLong(cursor)));
        } else {
            wrapper.lt(cursorColumn, parseCursor(cursor, cursorType));
        }

        wrapper.orderByDesc(cursorColumn);
        constraint.accept(wrapper);

        wrapper.last("limit 1");
        T one = mapper.getOne(wrapper);
        if (one == null) {
            isLast = Boolean.TRUE;
        }
        return isLast;
    }


    /**
     * 将游标【id | 时间戳】转化为字符串
     */
    private static String toCursor(Object obj) {
        if (obj instanceof Date) {
            long time = ((Date) obj).getTime();
            return String.valueOf(time);
        } else {
            return obj.toString();
        }
    }


    /**
     * 游标可以用id｜时间戳
     */
    private static Object parseCursor(String cursor, Class<?> cursorType) {
        if (Date.class.isAssignableFrom(cursorType)) {
            return new Date(Long.parseLong(cursor));
        }
        if (LocalDateTime.class.isAssignableFrom(cursorType)) {
            try {
                return LocalDateTime.parse(cursor);
            } catch (Exception e) {
                return LocalDateTime.parse(cursor.replace(" ", "T"));
            }
        }
        if (LocalDate.class.isAssignableFrom(cursorType)) {
            return LocalDate.parse(cursor);
        }
        if (LocalTime.class.isAssignableFrom(cursorType)) {
            return LocalTime.parse(cursor);
        }
        if (Instant.class.isAssignableFrom(cursorType)) {
            try {
                return Instant.parse(cursor);
            } catch (Exception e) {
                return Instant.ofEpochMilli(Long.parseLong(cursor));
            }
        }
        if (Long.class.isAssignableFrom(cursorType) || long.class.isAssignableFrom(cursorType)) {
            return Long.parseLong(cursor);
        }
        if (Integer.class.isAssignableFrom(cursorType) || int.class.isAssignableFrom(cursorType)) {
            return Integer.parseInt(cursor);
        }
        if (Short.class.isAssignableFrom(cursorType) || short.class.isAssignableFrom(cursorType)) {
            return Short.parseShort(cursor);
        }
        if (Double.class.isAssignableFrom(cursorType) || double.class.isAssignableFrom(cursorType)) {
            return Double.parseDouble(cursor);
        }
        if (Float.class.isAssignableFrom(cursorType) || float.class.isAssignableFrom(cursorType)) {
            return Float.parseFloat(cursor);
        }
        return cursor;
    }
}
