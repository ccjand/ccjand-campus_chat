package com.ccj.campus.chat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2024-05-03 15:17
 * @Description
 */
@Getter
@AllArgsConstructor
public enum TerminalTypeEnum {

    MOBILE_APP(1, "移动端app"),
    DESKTOP_APP(2, "桌面端app"),
    TABLET_APP(3, "平板端app"),
    MINI(4, "h5端");

    private final Integer type;
    private final String desc;

    final static Map<Integer, TerminalTypeEnum> cache;

    static {
        cache = Arrays.stream(TerminalTypeEnum.values()).collect(Collectors.toMap(TerminalTypeEnum::getType, v -> v));
    }

    public static TerminalTypeEnum of(Integer terminalType) {
        return cache.get(terminalType);
    }

    public static boolean support(Integer terminalType) {
        return cache.containsKey(terminalType);
    }

}
