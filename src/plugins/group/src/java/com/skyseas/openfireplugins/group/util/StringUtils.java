package com.skyseas.openfireplugins.group.util;

/**
 * Created by zhangzhi on 2014/9/1.
 */
public final class StringUtils {
    private StringUtils() {}

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String ifNullReturnEmpty(String str) {
        return str != null ? str : "";
    }


    public static String ifNullReturnDefaultValue(String str, String defaultValue) {
        return str != null ? str : defaultValue;
    }
}
