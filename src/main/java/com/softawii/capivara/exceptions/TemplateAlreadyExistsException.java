package com.softawii.capivara.exceptions;

public class TemplateAlreadyExistsException extends Exception {
    public TemplateAlreadyExistsException() {
    }

    public TemplateAlreadyExistsException(String message) {
        super(message);
    }

    public TemplateAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateAlreadyExistsException(Throwable cause) {
        super(cause);
    }

    public TemplateAlreadyExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
