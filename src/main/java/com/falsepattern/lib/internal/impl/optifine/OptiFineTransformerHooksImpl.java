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

package com.falsepattern.lib.internal.impl.optifine;

import com.falsepattern.lib.internal.FPLog;
import lombok.val;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OptiFineTransformerHooksImpl {
    private static final Set<String> disabledPatches = new HashSet<>();
    public static void init() {
        Field tField;
        try {
            tField = LaunchClassLoader.class.getDeclaredField("transformers");
        } catch (NoSuchFieldException e) {
            FPLog.LOG.error("Could not retrieve transformers field", e);
            return;
        }
        tField.setAccessible(true);
        List<IClassTransformer> transformers;
        try {
            transformers = (List<IClassTransformer>)tField.get(Launch.classLoader);
        } catch (IllegalAccessException e) {
            FPLog.LOG.error("Could not retrieve transformers list", e);
            return;
        }
        for (int i = 0; i < transformers.size(); i++) {
            val transformer = transformers.get(i);
            if (transformer.getClass().getName().equals("optifine.OptiFineClassTransformer")) {
                FPLog.LOG.info("Attaching OptiFine ASM transformer hooks");
                transformers.set(i, new WrappedOptiFineClassTransformer(transformer));
            }
        }
    }

    public static void disableOptiFinePatch(String patchName) {
        disabledPatches.add(patchName);
    }

    public static boolean isDisabled(String className) {
        return disabledPatches.contains(className);
    }
}
