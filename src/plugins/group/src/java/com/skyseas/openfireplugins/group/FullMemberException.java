package com.skyseas.openfireplugins.group;

/**
 * Created by apple on 14-8-31.
 */
public class FullMemberException extends Exception {
    public FullMemberException(String message) {
        super(message);
    }
    public FullMemberException(String message, Throwable cause) {
        super(message, cause);
    }
    public FullMemberException(Throwable cause) {
        super(cause);
    }
}
