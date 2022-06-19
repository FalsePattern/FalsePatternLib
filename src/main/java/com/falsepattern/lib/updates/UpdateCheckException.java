package com.falsepattern.lib.updates;

import com.falsepattern.lib.StableAPI;

@StableAPI(since = "0.8.3")
public class UpdateCheckException extends Exception {
    public UpdateCheckException(String message) {
        super(message);
    }

    public UpdateCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpdateCheckException(Throwable cause) {
        super(cause);
    }

    protected UpdateCheckException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
