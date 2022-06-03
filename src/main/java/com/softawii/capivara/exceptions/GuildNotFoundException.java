package com.softawii.capivara.exceptions;

public class GuildNotFoundException extends Exception {

    public GuildNotFoundException() {
        super();
    }

    public GuildNotFoundException(String message) {
        super(message);
    }

    public GuildNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GuildNotFoundException(Throwable cause) {
        super(cause);
    }

    protected GuildNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
