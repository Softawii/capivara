package com.softawii.capivara.exceptions;

public class CategoryIsEmptyException extends Exception {
    public CategoryIsEmptyException() {
    }

    public CategoryIsEmptyException(String message) {
        super(message);
    }

    public CategoryIsEmptyException(String message, Throwable cause) {
        super(message, cause);
    }

    public CategoryIsEmptyException(Throwable cause) {
        super(cause);
    }

    public CategoryIsEmptyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
