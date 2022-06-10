package com.softawii.capivara.exceptions;

public class FieldLengthException extends Exception {

    public FieldLengthException() {
    }

    public FieldLengthException(String message) {
        super(message);
    }

    public FieldLengthException(String message, Throwable cause) {
        super(message, cause);
    }

    public FieldLengthException(Throwable cause) {
        super(cause);
    }

    public FieldLengthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
