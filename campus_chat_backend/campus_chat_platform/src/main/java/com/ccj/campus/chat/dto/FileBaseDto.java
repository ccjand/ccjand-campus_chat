package com.ccj.campus.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author ccj
 * @Date 2024-05-09 21:43
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileBaseDto implements Serializable {

    /**
     * 文件大小（字节byte）
     */
    private Long size;

    /**
     * 文件下载地址
     */
    private String url;

}
