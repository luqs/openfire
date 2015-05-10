package com.skyseas.openfireplugins.group;

/**
 * Created by apple on 14-9-14.
 */
public class NoPermissionException extends Exception {
    public NoPermissionException() {

    }
    public NoPermissionException(String message) {
        super(message);
    }
}
