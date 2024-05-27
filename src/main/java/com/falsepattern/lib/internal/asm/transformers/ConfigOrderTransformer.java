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

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.impl.config.DeclOrderInternal;
import com.falsepattern.lib.turboasm.ClassNodeHandle;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ConfigOrderTransformer implements TurboClassTransformer {
    private static final String DESC_CONFIG = Type.getDescriptor(Config.class);
    private static final String DESC_CONFIG_IGNORE = Type.getDescriptor(Config.Ignore.class);
    private static final String DESC_ORDER = Type.getDescriptor(DeclOrderInternal.class);

    @Override
    public String name() {
        return "ConfigOrderTransformer";
    }

    @Override
    public String owner() {
        return Tags.MODNAME;
    }

    @Override
    public boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        val cn = classNode.getNode();
        if (cn == null)
            return false;
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
    public boolean transformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        val cn = classNode.getNode();
        if (cn == null)
            return false;
        int order = 0;
        boolean changed = false;
        outer:
        for (val field : cn.fields) {
            if ((field.access & Opcodes.ACC_PUBLIC) == 0
                || (field.access & Opcodes.ACC_STATIC) == 0
                || (field.access & Opcodes.ACC_FINAL) != 0) {
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
            changed = true;
        }
        return changed;
    }
}
