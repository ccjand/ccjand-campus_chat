package com.ccj.campus.chat.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


/**
 * 游标翻页返回
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursorPageBaseResp<T> {

    /**
     * 游标（下次翻页带上这参数）
     */
    private String cursor;

    /**
     * 是否最后一页
     */
    private Boolean isLast = Boolean.FALSE;

    /**
     * 数据列表
     */
    private List<T> list;

    private Integer offset = 1;

    public CursorPageBaseResp(String cursor, Boolean isLast, List<T> list) {
        this.cursor = cursor;
        this.isLast = isLast;
        this.list = list;
    }

    public static <T> CursorPageBaseResp<T> init(CursorPageBaseResp<?> cursorPage, List<T> list) {
        CursorPageBaseResp<T> cursorPageBaseResp = new CursorPageBaseResp<>();
        cursorPageBaseResp.setIsLast(cursorPage.getIsLast());
        cursorPageBaseResp.setList(list);
        cursorPageBaseResp.setCursor(cursorPage.getCursor());
        cursorPageBaseResp.setOffset(cursorPage.getOffset());
        return cursorPageBaseResp;
    }

    @JsonIgnore
    public Boolean isEmpty() {
        return list == null || list.size() == 0;
    }

    public static <T> CursorPageBaseResp<T> empty() {
        CursorPageBaseResp<T> cursorPageBaseResp = new CursorPageBaseResp<T>();
        cursorPageBaseResp.setIsLast(true);
        cursorPageBaseResp.setList(new ArrayList<>());
        return cursorPageBaseResp;
    }

}
