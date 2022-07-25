package com.falsepattern.lib.asm.exceptions;

public class AsmFieldNotFoundException extends AsmTransformException {
    public AsmFieldNotFoundException(final String field) {
        super("can't find field " + field);
    }
}
