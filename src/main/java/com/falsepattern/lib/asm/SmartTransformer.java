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
package com.falsepattern.lib.asm;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.asm.CoreLoadingPlugin;
import lombok.val;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import net.minecraft.launchwrapper.IClassTransformer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * An ASM transformation dispatcher utility, inspired by mixins.
 */
@StableAPI(since = "0.10.0")
public interface SmartTransformer extends IClassTransformer {
    @StableAPI.Expose
    Logger logger();

    @StableAPI.Expose
    List<IClassNodeTransformer> transformers();

    @Override
    default byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        val transformers = new ArrayList<IClassNodeTransformer>();
        val cn = ASMUtil.parseClass(bytes, ClassReader.EXPAND_FRAMES);
        for (val transformer : transformers()) {
            if (transformer.shouldTransform(cn, transformedName, CoreLoadingPlugin.isObfuscated())) {
                transformers.add(transformer);
            }
        }
        if (transformers.isEmpty()) {
            return bytes;
        }
        transformers.sort(Comparator.comparingInt(IClassNodeTransformer::internalSortingOrder));
        val log = logger();
        for (val transformer : transformers) {
            log.debug("Patching {} with {}...", transformedName, transformer.getName());
            try {
                transformer.transform(cn, transformedName, CoreLoadingPlugin.isObfuscated());
            } catch (RuntimeException | Error t) {
                log.error("Error transforming {} with {}: {}", transformedName, transformer.getName(), t.getMessage());
                throw t;
            } catch (Throwable t) {
                log.error("Error transforming {} with {}: {}", transformedName, transformer.getName(), t.getMessage());
                throw new RuntimeException(t);
            }
        }
        val result = ASMUtil.serializeClass(cn, ClassWriter.COMPUTE_FRAMES);
        log.debug("Patched {} successfully.", transformedName);
        return result;
    }
}
