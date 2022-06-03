package com.softawii.capivara.exceptions;

public class MultipleEmojiMessageException extends Exception {

    public MultipleEmojiMessageException() {
    }

    public MultipleEmojiMessageException(String message) {
        super(message);
    }

    public MultipleEmojiMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleEmojiMessageException(Throwable cause) {
        super(cause);
    }

    public MultipleEmojiMessageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
