package com.softawii.capivara.exceptions;

public class KeyAlreadyInPackageException extends Exception {

    public KeyAlreadyInPackageException() {
    }

    public KeyAlreadyInPackageException(String message) {
        super(message);
    }

    public KeyAlreadyInPackageException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyAlreadyInPackageException(Throwable cause) {
        super(cause);
    }

    public KeyAlreadyInPackageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
