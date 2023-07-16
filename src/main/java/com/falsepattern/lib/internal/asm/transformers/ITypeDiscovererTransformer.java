/*
 * Copyright (C) 2022-2023 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
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
import lombok.val;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class ITypeDiscovererTransformer implements IClassNodeTransformer {
    @Override
    public String getName() {
        return "ITypeDiscovererTransformer";
    }

    @Override
    public boolean shouldTransform(ClassNode cn, String transformedName, boolean obfuscated) {
        return "cpw.mods.fml.common.discovery.ITypeDiscoverer".equals(transformedName);
    }

    @Override
    public void transform(ClassNode cn, String transformedName, boolean obfuscated) {
        for (val method: cn.methods) {
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
                        return;
                    }
                }
            }
        }
    }
}
