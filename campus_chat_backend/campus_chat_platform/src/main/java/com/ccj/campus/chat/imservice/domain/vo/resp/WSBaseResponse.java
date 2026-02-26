package com.ccj.campus.chat.imservice.domain.vo.resp;

import com.ccj.campus.chat.imservice.enums.WSResponseTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author ccj
 * @Date 2024-05-03 17:43
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WSBaseResponse<T> {
    /**
     * @see com.ccj.campus.chat.imservice.enums.WSResponseTypeEnum
     */
    private Integer type;
    private T data;

    public WSBaseResponse(WSResponseTypeEnum typeEnum, T data) {
        this.setType(typeEnum.getType());
        this.setData(data);
    }

    public WSBaseResponse(WSResponseTypeEnum typeEnum) {
        this.setType(typeEnum.getType());
    }
}
