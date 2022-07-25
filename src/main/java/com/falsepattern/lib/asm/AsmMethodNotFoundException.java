package com.falsepattern.lib.asm;

public class AsmMethodNotFoundException extends AsmTransformException {
    public AsmMethodNotFoundException(final String method) {
        super("can't find method " + method);
    }
}
