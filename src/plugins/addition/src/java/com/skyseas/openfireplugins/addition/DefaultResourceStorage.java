package com.skyseas.openfireplugins.addition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 默认的资源存储器。
 * Created by zhangzhi on 2014/11/24.
 */
public final class DefaultResourceStorage implements ResourceStorage {
    private final Logger LOGGER = LoggerFactory.getLogger(DefaultResourceStorage.class);
    private final File basePath;

    public DefaultResourceStorage(File basePath) throws IOException {
        if (basePath == null) {
            throw new NullPointerException("basePath is null.");
        }
        if (basePath.isFile()) {
            throw new IllegalArgumentException("basePath must be an existing directory.");
        }
        if (!basePath.exists() && !basePath.mkdirs()) {
            LOGGER.info("create basepatch fail:" + basePath);
            throw new IOException("create basePath fail.");
        }
        this.basePath = basePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String store(InputStream stream) throws IOException {
        if (stream == null) {
            throw new NullPointerException("stream");
        }

        String resName = nextResName();
        OutputStream out = null;

        try {
            out = createFile(resName);
            Util.writeTo(stream, out);
        } catch (Exception exp) {
            LOGGER.error(String.format("write to resourceName : %s fail.", resName), exp);
        } finally {
            out.close();
        }
        return resName;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Resource load(String resourceName) throws IOException {
        if (resourceName == null || resourceName.length() == 0 || resourceName.contains("..")) {
            throw new IllegalArgumentException("resourceName");
        }

        File resFile = new File(basePath, resourceName);
        if (resFile.exists()) {
            return new DefaultResource(resFile, resourceName);
        }
        return null;
    }

    /**
     * 创建资源文件。
     *
     * @param resName
     * @return
     * @throws IOException
     */
    private OutputStream createFile(String resName) throws IOException {

        File file = new File(basePath, resName);
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new FileOutputStream(file, false);
    }

    /**
     * 生成下一个资源名称。
     *
     * @return
     */
    private String nextResName() {
        Calendar now = Calendar.getInstance();
        return
                now.get(Calendar.YEAR) + "/" +
                        now.get(Calendar.MONTH) + "/" +
                        now.get(Calendar.DAY_OF_MONTH) + "/" + Util.nextRandomString();
    }
}
