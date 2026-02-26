package com.ccj.campus.chat.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * @Author ccj
 * @Date 2024-05-02 17:16
 * @Description
 */
public class BCryptUtil {

    public static String encode(String password, String salt) {
        return BCrypt.hashpw(password, salt);
    }

    public static String encode(String password) {
        return encode(password, BCrypt.gensalt());
    }

    public static boolean check(String original, String encoded) {
        return BCrypt.checkpw(original, encoded);
    }

    public static void main(String[] args) {
        System.out.println(encode("123456"));
    }
}
