package com.skyseas.openfireplugins.group.spi;

/**
 * Created by zhangzhi on 2014/8/27.
 */
public class PersistenceException extends Exception {
    public PersistenceException(String message) {
        super(message);
    }
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
    public PersistenceException(Throwable cause) {
        super(cause);
    }
}
