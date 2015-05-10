package com.skyseas.openfireplugins.addition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 默认的资源对象。
 * Created by zhangzhi on 2014/11/24.
 */
final class DefaultResource implements Resource {
    private final File resFile;
    private final String name;

    public DefaultResource(File resFile, String name) {
        assert resFile != null;
        this.resFile = resFile;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        return resFile.length();
    }

    @Override
    public InputStream open() throws IOException {
        return new FileInputStream(resFile);
    }
}
