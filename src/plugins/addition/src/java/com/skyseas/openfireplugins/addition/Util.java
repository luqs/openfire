package com.skyseas.openfireplugins.addition;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by zhangzhi on 2014/11/24.
 */
public final class Util {
    private static final SecureRandom random = new SecureRandom();

    private Util() {
    }

    public static void writeTo(InputStream source, OutputStream dest) throws IOException {
        byte[] buffer = new byte[256];
        int length;
        while ((length = source.read(buffer)) != -1) {
            dest.write(buffer, 0, length);
        }
    }

    public static String nextRandomString() {
        return new BigInteger(130, random).toString(32);
    }
}
