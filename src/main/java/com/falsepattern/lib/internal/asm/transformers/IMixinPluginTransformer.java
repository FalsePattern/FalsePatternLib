/*
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lib.internal.asm.transformers;

import com.falsepattern.lib.asm.IClassNodeTransformer;
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.asm.FPTransformer;
import lombok.val;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.HashMap;
import java.util.Map;

public class IMixinPluginTransformer implements IClassNodeTransformer {
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
    public String getName() {
        return "IMixinPluginTransformer";
    }

    @Override
    public boolean shouldTransform(ClassNode cn, String transformedName, boolean obfuscated) {
        return IMIXINPLUGIN.equals(transformedName) ||
               IMIXINCONFIGPLUGIN.equals(transformedName) ||
               cn.interfaces.stream().anyMatch((i) -> IMIXINPLUGIN_INTERNAL.equals(i) ||
                                                      IMIXINCONFIGPLUGIN_INTERNAL.equals(i));
    }

    @Override
    public void transform(ClassNode cn, String transformedName, boolean obfuscated) {
        if (IMIXINCONFIGPLUGIN.equals(transformedName)) {
            extractMixinConfigPluginData(cn);
        } else {
            transformPlugin(cn, transformedName);
        }
    }

    private static void extractMixinConfigPluginData(ClassNode cn) {
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
                for (val local: method.localVariables) {
                    if (local.desc.contains("ClassNode;")) {
                        local.desc = local.desc.replaceAll("L[a-zA-Z/$]+[a-zA-Z$]+/ClassNode;", CLASSNODE_REAL);
                    }
                }
            }
        }
    }

    private static void transformPlugin(ClassNode cn, String transformedName) {
        FPTransformer.LOG.info("Transforming " + transformedName + " to fit current mixin environment.");
        if (PREAPPLY_DESC == null) {
            PREAPPLY_DESC = extractMethodWithReflection("preApply");
        }
        if (POSTAPPLY_DESC == null) {
            POSTAPPLY_DESC = extractMethodWithReflection("postApply");
        }
        for (val method : cn.methods) {
            switch (method.name) {
                case "preApply":
                    method.desc = PREAPPLY_DESC;
                    break;
                case "postApply":
                    method.desc = POSTAPPLY_DESC;
                    break;
            }
        }
    }

    private static String extractMethodWithReflection(String m) {
        for (val method: IMixinConfigPlugin.class.getDeclaredMethods()) {
            if (method.getName().equals(m)) {
                StringBuilder b = new StringBuilder("(");
                for (val param: method.getParameterTypes()) {
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
