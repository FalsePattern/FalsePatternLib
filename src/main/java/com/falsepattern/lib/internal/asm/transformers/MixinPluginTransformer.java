/*
 * This file is part of FalsePatternLib.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalsePatternLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalsePatternLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalsePatternLib. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lib.internal.asm.transformers;

import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.asm.FPTransformer;
import com.falsepattern.lib.turboasm.ClassNodeHandle;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.HashMap;
import java.util.Map;

public class MixinPluginTransformer implements TurboClassTransformer {
    private static final String IMIXINPLUGIN = Tags.GROUPNAME + ".mixin.IMixinPlugin";
    private static final String IMIXINPLUGIN_INTERNAL = IMIXINPLUGIN.replace('.', '/');
    private static final String IMIXINCONFIGPLUGIN = "org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin";
    private static final String IMIXINCONFIGPLUGIN_INTERNAL = IMIXINCONFIGPLUGIN.replace('.', '/');
    private static final Map<Class<?>, String> primitiveDescriptors = new HashMap<>();

    private static String PREAPPLY_DESC = null;
    private static String POSTAPPLY_DESC = null;
    private static String CLASSNODE_REAL = null;

    static {
        primitiveDescriptors.put(Void.TYPE, "V");
        primitiveDescriptors.put(Boolean.TYPE, "Z");
        primitiveDescriptors.put(Byte.TYPE, "B");
        primitiveDescriptors.put(Short.TYPE, "S");
        primitiveDescriptors.put(Integer.TYPE, "I");
        primitiveDescriptors.put(Long.TYPE, "J");
        primitiveDescriptors.put(Float.TYPE, "F");
        primitiveDescriptors.put(Double.TYPE, "D");
        primitiveDescriptors.put(Character.TYPE, "C");
    }

    @Override
    public String owner() {
        return Tags.MODNAME;
    }

    @Override
    public String name() {
        return "MixinPluginTransformer";
    }

    @Override
    public boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        if (IMIXINPLUGIN.equals(className) ||
            IMIXINCONFIGPLUGIN.equals(className))
            return true;

        if (!classNode.isPresent())
            return false;

        if (classNode.isOriginal()) {
            val meta = classNode.getOriginalMetadata();
            if (meta != null && meta.interfacesCount == 0)
                return false;
        }

        val cn = classNode.getNode();
        if (cn == null)
            return false;

        for (String i : cn.interfaces) {
            if (IMIXINPLUGIN_INTERNAL.equals(i) || IMIXINCONFIGPLUGIN_INTERNAL.equals(i)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean transformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        val cn = classNode.getNode();
        if (cn == null)
            return false;
        if (IMIXINCONFIGPLUGIN.equals(className)) {
            return extractMixinConfigPluginData(cn);
        } else {
            return transformPlugin(cn);
        }
    }

    private static boolean extractMixinConfigPluginData(ClassNode cn) {
        boolean changed = false;
        for (val method : cn.methods) {
            switch (method.name) {
                case "preApply":
                    PREAPPLY_DESC = method.desc;
                    break;
                case "postApply":
                    POSTAPPLY_DESC = method.desc;
                    break;
                default:
                    continue;
            }
            if (CLASSNODE_REAL != null) {
                for (val local : method.localVariables) {
                    if (local.desc.contains("ClassNode;")) {
                        local.desc = local.desc.replaceAll("L[a-zA-Z/$]+[a-zA-Z$]+/ClassNode;", CLASSNODE_REAL);
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private static boolean transformPlugin(ClassNode cn) {
        if (PREAPPLY_DESC == null) {
            PREAPPLY_DESC = extractMethodWithReflection("preApply");
        }
        if (POSTAPPLY_DESC == null) {
            POSTAPPLY_DESC = extractMethodWithReflection("postApply");
        }
        boolean changed = false;
        for (val method : cn.methods) {
            switch (method.name) {
                case "preApply":
                    if (!PREAPPLY_DESC.equals(method.desc)) {
                        method.desc = PREAPPLY_DESC;
                        changed = true;
                    }
                    break;
                case "postApply":
                    if (!POSTAPPLY_DESC.equals(method.desc)) {
                        method.desc = POSTAPPLY_DESC;
                        changed = true;
                    }
                    break;
            }
        }
        return changed;
    }

    private static String extractMethodWithReflection(String m) {
        for (val method : IMixinConfigPlugin.class.getDeclaredMethods()) {
            if (method.getName().equals(m)) {
                StringBuilder b = new StringBuilder("(");
                for (val param : method.getParameterTypes()) {
                    if (param.getName().contains("ClassNode")) {
                        CLASSNODE_REAL = "L" + param.getName().replace('.', '/') + ";";
                    }
                    b.append(classToRaw(param));
                }
                b.append(")").append(classToRaw(method.getReturnType()));
                return b.toString();
            }
        }
        throw new RuntimeException("Could not extract " + m + " from IMixinConfigPlugin!");
    }

    private static String classToRaw(Class<?> clazz) {
        if (primitiveDescriptors.containsKey(clazz)) {
            return primitiveDescriptors.get(clazz);
        } else {
            return "L" + clazz.getName().replace('.', '/') + ";";
        }
    }
}
