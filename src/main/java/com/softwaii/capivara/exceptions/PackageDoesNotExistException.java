package com.softwaii.capivara.exceptions;

public class PackageDoesNotExistException extends Exception {

    public PackageDoesNotExistException() {
        super();
    }

    public PackageDoesNotExistException(String message) {
        super(message);
    }

    public PackageDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public PackageDoesNotExistException(Throwable cause) {
        super(cause);
    }

    protected PackageDoesNotExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
