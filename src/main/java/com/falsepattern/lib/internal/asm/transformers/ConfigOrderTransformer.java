package com.falsepattern.lib.internal.asm.transformers;

import com.falsepattern.lib.asm.IClassNodeTransformer;
import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.internal.impl.config.DeclOrderInternal;
import lombok.val;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

public class ConfigOrderTransformer implements IClassNodeTransformer {
    private static final String DESC_CONFIG = Type.getDescriptor(Config.class);
    private static final String DESC_CONFIG_IGNORE = Type.getDescriptor(Config.Ignore.class);
    private static final String DESC_ORDER = Type.getDescriptor(DeclOrderInternal.class);
    @Override
    public String getName() {
        return "ConfigOrderTransformer";
    }

    @Override
    public boolean shouldTransform(ClassNode cn, String transformedName, boolean obfuscated) {
        if (cn.visibleAnnotations != null) {
            for (val ann : cn.visibleAnnotations) {
                if (DESC_CONFIG.equals(ann.desc)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void transform(ClassNode cn, String transformedName, boolean obfuscated) {
        int order = 0;
        outer:
        for (val field: cn.fields) {
            if ((field.access & Opcodes.ACC_PUBLIC) == 0 ||
                (field.access & Opcodes.ACC_STATIC) == 0 ||
                (field.access & Opcodes.ACC_FINAL) != 0) {
                    continue;
            } else if (field.visibleAnnotations != null) {
                for (val ann : field.visibleAnnotations) {
                    if (DESC_CONFIG_IGNORE.equals(ann.desc)) {
                        continue outer;
                    }
                }
            }
            val annVisitor = field.visitAnnotation(DESC_ORDER, true);
            annVisitor.visit("value", order++);
            annVisitor.visitEnd();
        }
    }
}
