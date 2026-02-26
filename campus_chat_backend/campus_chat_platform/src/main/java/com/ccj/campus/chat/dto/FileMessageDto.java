package com.ccj.campus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author ccj
 * @Date 2024-05-09 21:38
 * @Description 文件消息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMessageDto extends FileBaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件名，包含后缀
     */
    private String filename;


}
