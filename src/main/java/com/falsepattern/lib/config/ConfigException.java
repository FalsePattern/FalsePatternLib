package com.falsepattern.lib.config;

import com.falsepattern.lib.StableAPI;

/**
 * A really basic wrapper for config to simplify handling them in external code.
 */
@StableAPI(since = "0.6.0")
public class ConfigException extends Exception {

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(Throwable cause) {
        super(cause);
    }
}
