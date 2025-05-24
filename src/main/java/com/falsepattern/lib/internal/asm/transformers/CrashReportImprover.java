/*
 * This file is part of FalsePatternLib.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalsePatternLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
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
import com.falsepattern.lib.turboasm.ClassNodeHandle;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class CrashReportImprover implements TurboClassTransformer {

    @Override
    public String owner() {
        return Tags.MODNAME;
    }

    @Override
    public String name() {
        return "CrashReportImprover";
    }

    @Override
    public boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        return "net.minecraft.crash.CrashReport".equals(className);
    }

    @Override
    public boolean transformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        val cn = classNode.getNode();
        if (cn == null)
            return false;
        for (val method: cn.methods) {
            switch (method.name) {
                case "saveToFile", "func_147149_a" -> {}
                default -> {
                    continue;
                }
            }
            val insnList = method.instructions.iterator();
            while (insnList.hasNext()) {
                val insn = insnList.next();
                if ( insn.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                     insn instanceof MethodInsnNode call &&
                     "java/io/FileWriter".equals(call.owner) &&
                     "close".equals(call.name) &&
                     "()V".equals(call.desc)) {
                    insnList.previous();
                    insnList.add(new InsnNode(Opcodes.DUP));
                    insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/falsepattern/lib/internal/logging/CrashImprover", "injectLatest", "(Ljava/io/FileWriter;)V", false));
                    return true;
                }
            }
        }
        return false;
    }
}
