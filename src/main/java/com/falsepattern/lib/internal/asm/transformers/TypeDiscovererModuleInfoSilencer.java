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

import com.falsepattern.lib.turboasm.ClassNodeHandle;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class TypeDiscovererModuleInfoSilencer implements TurboClassTransformer {

    @Override
    public String owner() {
        return "FalsePatternLib";
    }

    @Override
    public String name() {
        return "TypeDiscovererModuleInfoSilencer";
    }

    @Override
    public boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        return "cpw.mods.fml.common.discovery.ITypeDiscoverer".equals(className);
    }

    @Override
    public boolean transformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        val cn = classNode.getNode();
        if (cn == null)
            return false;
        for (val method : cn.methods) {
            if (!method.name.equals("<clinit>")) {
                continue;
            }
            val instructions = method.instructions;
            for (int i = 0; i < instructions.size(); i++) {
                val insn = instructions.get(i);
                if (insn instanceof LdcInsnNode) {
                    val ldc = (LdcInsnNode) insn;
                    if (ldc.cst.equals("[^\\s\\$]+(\\$[^\\s]+)?\\.class$")) {
                        ldc.cst = "(?!module-info)[^\\s\\$]+(\\$[^\\s]+)?\\.class$";
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
