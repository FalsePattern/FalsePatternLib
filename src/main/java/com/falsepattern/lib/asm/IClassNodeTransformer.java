package com.falsepattern.lib.asm;

import org.objectweb.asm.tree.ClassNode;

public interface IClassNodeTransformer {
    String getName();
    boolean shouldTransform(ClassNode cn, String transformedName, boolean obfuscated);
    default int internalSortingOrder() {
        return 0;
    }

    void transform(ClassNode cn, String transformedName, boolean obfuscated);
}
