package com.falsepattern.lib.asm;

public class AsmTransformException extends RuntimeException {
    public AsmTransformException(final String message) {
        super(message);
    }

    public AsmTransformException(final Throwable cause) {
        super(cause);
    }

    public AsmTransformException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
