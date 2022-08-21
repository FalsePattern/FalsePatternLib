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
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.mixin.MixinInfo;
import lombok.val;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;

public class IMixinPluginTransformer implements IClassNodeTransformer {
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
        if (MixinInfo.isMixinBooterLegacy()) {
            FPTransformer.LOG.info("Detected MixinBooterLegacy. Selecting proper methods in IMixinPlugin.");
            for (val method : methods) {
                if (method.name.equals("preApply_obsolete") || method.name.equals("postApply_obsolete")) {
                    remove.add(method);
                }
            }
            methods.removeAll(remove);
        } else {
            FPTransformer.LOG.info("Could not detect MixinBooterLegacy. Selecting proper methods in IMixinPlugin.");
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
        }
    }
}
