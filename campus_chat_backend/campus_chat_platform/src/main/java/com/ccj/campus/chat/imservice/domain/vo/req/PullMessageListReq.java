package com.ccj.campus.chat.imservice.domain.vo.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Author ccj
 * @Date 2024-07-20 15:58
 * @Description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PullMessageListReq implements Serializable {

    /**
     * 一次拉取多少条消息
     */
    @Min(1)
    @Max(200)
    @NotNull
    private Integer batchSize;

    /**
     * key: roomId
     * value: lastMsgId
     */
    @NotNull
    private Map<Long, Long> roomIdLastMsgIdMap;

    @JsonIgnore
    public <T> Page<T> pagePlus() {
        return new Page<>(1, batchSize, false);
    }
}
