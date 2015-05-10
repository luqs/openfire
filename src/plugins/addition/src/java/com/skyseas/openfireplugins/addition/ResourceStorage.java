package com.skyseas.openfireplugins.addition;

import java.io.IOException;
import java.io.InputStream;

/**
 * 资源存储器接口。
 * Created by zhangzhi on 2014/11/21.
 */
public interface ResourceStorage {

    /**
     * 将数据流存储到存储器。
     * @param stream
     * @return 资源名称。
     * @throws IOException
     */
    String store(InputStream stream) throws IOException;

    /**
     * 通过资源名称加载资源对象。
     * @param resourceName 资源名称。
     * @return 资源对象。
     * @throws IOException
     */
    Resource load(String resourceName) throws IOException;
}
