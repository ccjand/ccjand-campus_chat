package com.ccj.campus.chat.enums;

import com.ccj.campus.chat.imservice.enums.TerminalTypeEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author ccj
 * @Date 2026-01-14 18:49
 * @Description 学院
 */
public enum DepartmentType {

    /**
     * 信息工程学院
     */
    INFO_ENGINEERING(1L, "信息工程学院"),
    /**
     * 软件学院
     */
    SOFTWARE_ENGINEERING(2L, "软件学院"),
    /**
     * 电子信息学院
     */
    ELECTRONIC_INFORMATION(3L, "电子信息学院"),
    /**
     * 管理学院
     */
    MANAGEMENT(4L, "管理学院"),
    /**
     * 经济学院
     */
    ECONOMICS(5L, "经济学院"),
    /**
     * 法律学院
     */
    LAW(6L, "法律学院"),
    /**
     * 设计学院
     */
    DESIGN(7L, "设计学院"),
    /**
     * 艺术学院
     */
    ARTS(8L, "艺术学院");


    private final Long code;
    private final String value;

    DepartmentType(Long code, String value) {
        this.code = code;
        this.value = value;
    }

    public Long getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    final static Map<Long, DepartmentType> cache;

    static {
        cache = Arrays.stream(DepartmentType.values()).collect(Collectors.toMap(DepartmentType::getCode, v -> v));
    }


    public static String getDepartmentName(Long code) {
        return cache.get(code).getValue();
    }

}

