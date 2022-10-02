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

package com.falsepattern.lib.internal.asm;

import com.falsepattern.lib.asm.IClassNodeTransformer;
import lombok.val;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class NonUpdateTransformer implements IClassNodeTransformer {
    @Override
    public String getName() {
        return "NonUpdateTransformer";
    }

    @Override
    public boolean shouldTransform(ClassNode cn, String transformedName, boolean obfuscated) {
        return "moe.mickey.forge.nonupdate.NonUpdate".equals(transformedName);
    }

    @Override
    public void transform(ClassNode cn, String transformedName, boolean obfuscated) {
        MethodNode toRemove = null;
        for (val method: cn.methods) {
            if (method.name.equals("init")) {
                toRemove = method;
                break;
            }
        }
        cn.methods.remove(toRemove);
    }
}
