package com.softawii.capivara.exceptions;

public class TemplateDoesNotExistException extends Exception {
    public TemplateDoesNotExistException() {
    }

    public TemplateDoesNotExistException(String message) {
        super(message);
    }

    public TemplateDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateDoesNotExistException(Throwable cause) {
        super(cause);
    }

    public TemplateDoesNotExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
