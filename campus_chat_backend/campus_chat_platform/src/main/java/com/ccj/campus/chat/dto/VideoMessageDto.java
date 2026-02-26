package com.ccj.campus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author ccj
 * @Date 2024-05-09 21:48
 * @Description
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoMessageDto extends FileBaseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 视频封面展示的宽度(单位是像素)
     */
    private Integer coverWidth;

    /**
     * 视频封面展示的高度(单位是像素)
     */
    private Integer coverHeight;

    /**
     * 视频封面图片的大小，单位：字节。
     */
    private Long coverSize;

    /**
     * 视频缩略图下载地址。可通过该 URL 地址直接下载相应视频缩略图。
     */
    private String coverUrl;

    /**
     * 视频时长，单位：秒。
     */
    private Long second;
}
