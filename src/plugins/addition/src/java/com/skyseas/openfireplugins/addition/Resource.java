package com.skyseas.openfireplugins.addition;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhangzhi on 2014/11/24.
 */
public interface Resource {
    String getName();
    long getSize();
    InputStream open() throws IOException;
}
