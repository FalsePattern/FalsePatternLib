package com.falsepattern.lib.internal.asm;

import com.falsepattern.lib.asm.IClassNodeTransformer;
import com.falsepattern.lib.internal.Tags;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;

@RequiredArgsConstructor
public class IMixinPluginTransformer implements IClassNodeTransformer {
    private final boolean obsolete;

    @Override
    public String getName() {
        return "IMixinPluginTransformer";
    }

    @Override
    public boolean shouldTransform(ClassNode cn, String transformedName, boolean obfuscated) {
        return transformedName.equals(Tags.GROUPNAME + ".mixin.IMixinPlugin");
    }

    @Override
    public void transform(ClassNode cn, String transformedName, boolean obfuscated) {
        val methods = cn.methods;
        val remove = new ArrayList<MethodNode>();
        if (obsolete) {
            for (val method : methods) {
                if (method.name.equals("preApply") || method.name.equals("postApply")) {
                    remove.add(method);
                }
            }
            methods.removeAll(remove);
            for (val method : methods) {
                if (method.name.equals("preApply_obsolete") || method.name.equals("postApply_obsolete")) {
                    method.name = method.name.substring(0, method.name.indexOf('_'));
                }
            }
        } else {
            for (val method : methods) {
                if (method.name.equals("preApply_obsolete") || method.name.equals("postApply_obsolete")) {
                    remove.add(method);
                }
            }
        }
    }
}
