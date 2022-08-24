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

import java.util.List;

public class IMixinPluginTransformer implements IClassNodeTransformer {
    private static final String CLASSNODE_MIXINBOOTER = "org/spongepowered/libraries/org/objectweb/asm/tree/ClassNode";
    private static final String CLASSNODE_SPONGEMIXINS = "org/spongepowered/asm/lib/tree/ClassNode";
    private static final String IMIXINPLUGIN = Tags.GROUPNAME + ".mixin.IMixinPlugin";
    private static final String IMIXINPLUGIN_INTERNAL = IMIXINPLUGIN.replace('.', '/');
    private static final String IMIXINCONFIGPLUGIN_INTERNAL = "org/spongepowered/asm/mixin/extensibility/IMixinConfigPlugin";
    @Override
    public String getName() {
        return "IMixinPluginTransformer";
    }

    @Override
    public boolean shouldTransform(ClassNode cn, String transformedName, boolean obfuscated) {
        return transformedName.equals(IMIXINPLUGIN) ||
               cn.interfaces.stream().anyMatch((i) -> i.equals(IMIXINPLUGIN_INTERNAL) || i.equals(IMIXINCONFIGPLUGIN_INTERNAL));
    }

    @Override
    public void transform(ClassNode cn, String transformedName, boolean obfuscated) {
        if (transformedName.equals(IMIXINPLUGIN)) {
            transformIMixinPlugin(cn);
        } else {
            transformPlugin(cn, transformedName);
        }
    }

    private static void transformIMixinPlugin(ClassNode cn) {
        if (!MixinInfo.isMixinBooterLegacy()) {
            FPTransformer.LOG.info("Could not detect MixinBooterLegacy. Converting IMixinPlugin to legacy compat mode.");
            doRename(cn.methods, CLASSNODE_MIXINBOOTER, CLASSNODE_SPONGEMIXINS);
        }
    }

    private static void transformPlugin(ClassNode cn, String transformedName) {
        FPTransformer.LOG.info("Transforming " + transformedName + " to fit current mixin environment.");
        if (!MixinInfo.isMixinBooterLegacy()) {
            doRename(cn.methods, CLASSNODE_MIXINBOOTER, CLASSNODE_SPONGEMIXINS);
        } else {
            doRename(cn.methods, CLASSNODE_SPONGEMIXINS, CLASSNODE_MIXINBOOTER);
        }
    }

    private static void doRename(List<MethodNode> methods, String from, String to) {
        for (val method : methods) {
            if (method.name.equals("preApply") || method.name.equals("postApply")) {
                val newDesc = method.desc.replace(from, to);
                if (!method.desc.equals(newDesc)) {
                    FPTransformer.LOG.debug(method.name + method.desc + " -> " + method.name + newDesc);
                }
                method.desc = newDesc;
                for (val local: method.localVariables) {
                    local.desc = local.desc.replace(from, to);
                }
            }
        }
    }
}
