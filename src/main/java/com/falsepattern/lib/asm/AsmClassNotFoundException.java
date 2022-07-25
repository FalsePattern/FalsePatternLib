package com.falsepattern.lib.asm;

public class AsmClassNotFoundException extends AsmTransformException {
    public AsmClassNotFoundException(final String clazz) {
        super("can't find class " + clazz);
    }
}
