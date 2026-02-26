package com.ccj.campus.chat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author ccj
 * @Date 2024-04-10 14:41
 * @Description
 */
@AllArgsConstructor
@Getter
public enum YesOrNo {

    YES(1, "是"),
    NO(0, "否");


    private final Integer type;
    private final String desc;

    public static Integer parse(boolean res) {
        if (res) return YesOrNo.YES.type;
        else return YesOrNo.NO.type;
    }

    public static YesOrNo parseYesOrNo(boolean res) {
        if (res) return YesOrNo.YES;
        else return YesOrNo.NO;
    }

    public static Boolean parseYesOrNo(Integer val) {
        return YesOrNo.YES.type.equals(val);
    }

    public static Integer reverse(Integer val) {
        return YesOrNo.YES.type.equals(val) ? YesOrNo.NO.getType() : YesOrNo.YES.getType();
    }

    public static boolean judge(Integer val) {
        return YesOrNo.YES.type.equals(val);
    }
}
