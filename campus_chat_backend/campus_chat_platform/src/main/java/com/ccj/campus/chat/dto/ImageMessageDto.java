package com.ccj.campus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author ccj
 * @Date 2024-05-09 21:42
 * @Description 图片消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageMessageDto  implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer width;

    private Integer height;

    private String url;

    private Long size;
}
