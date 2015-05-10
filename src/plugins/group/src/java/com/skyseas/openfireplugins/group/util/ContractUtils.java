package com.skyseas.openfireplugins.group.util;

/**
 * Created by zhangzhi on 2014/9/1.
 */
 public class ContractUtils {

    public static void requires(boolean precondition, String paramName) {
        assert(!StringUtils.isNullOrEmpty(paramName));

        if (!precondition) {
            throw new IllegalArgumentException(String.format("%s invalid.", paramName));
        }
    }

    public static void requiresNotNull(Object obj, String paramName) {
        if(obj == null) {
            throw new NullPointerException(String.format("%s is null.", paramName));
        }
    }

    public static void requiresNotEmpty(String str, String paramName) {
        requiresNotNull(str, paramName);
        if(str.length() == 0) {
            throw new IllegalArgumentException(String.format("%s is empty.", paramName));
        }
    }

}
