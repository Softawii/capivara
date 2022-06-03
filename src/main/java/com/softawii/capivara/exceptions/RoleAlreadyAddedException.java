package com.softawii.capivara.exceptions;

public class RoleAlreadyAddedException extends Exception {
    public RoleAlreadyAddedException() {
    }

    public RoleAlreadyAddedException(String message) {
        super(message);
    }

    public RoleAlreadyAddedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RoleAlreadyAddedException(Throwable cause) {
        super(cause);
    }

    public RoleAlreadyAddedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
