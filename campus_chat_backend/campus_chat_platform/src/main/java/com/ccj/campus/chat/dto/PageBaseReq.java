package com.ccj.campus.chat.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 基础翻页请求
 */
@Data
public class PageBaseReq {

    /**
     * 页面大小
     */
    @Min(0)
    @Max(50)
    private Integer pageSize = 20;

    /**
     * 页面索引（从1开始）
     */
    private Integer pageNo = 1;

    /**
     * 获取mybatisPlus的page
     */
    public Page plusPage() {
        return new Page(pageNo, pageSize);
    }
}
