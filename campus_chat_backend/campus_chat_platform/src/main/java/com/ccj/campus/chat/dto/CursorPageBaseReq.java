package com.ccj.campus.chat.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Author ccj
 * @Date 2024-04-18 18:17
 * @Description 游标分页请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursorPageBaseReq implements Serializable {

    /**
     * 一页显示数
     */
    @Min(1)
    @Max(60)
    @NotNull
    private Integer pageSize;

    /**
     * 游标, 初始值为null表示第一页,后续请求都需要携带上次翻页的游标
     */
    private String cursor = null;

    /**
     * redis zset游标查询需要
     * mysql就不用
     */
    private Integer offset = 1;

    public CursorPageBaseReq(Integer pageSize, String cursor) {
        this.pageSize = pageSize;
        this.cursor = cursor;
    }

    @JsonIgnore
    public <T> Page<T> pagePlus() {
        return new Page<>(1, pageSize, false);
    }

    @JsonIgnore
    public Boolean isFirstPage() {
        return StringUtils.isBlank(cursor);
    }
}
