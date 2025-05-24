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
package com.falsepattern.lib.turboasm;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.asm.CoreLoadingPlugin;
import lombok.val;

import net.minecraft.launchwrapper.IClassTransformer;

import java.util.ArrayList;
import java.util.List;

@StableAPI(since = "__EXPERIMENTAL__")
public class MergeableTurboTransformer implements IClassTransformer {
    private final List<TurboClassTransformer> transformers;

    public MergeableTurboTransformer(List<TurboClassTransformer> transformers) {
        this.transformers = new ArrayList<>(transformers);
    }

    @Override
    public final byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (transformers.isEmpty()) {
            return bytes;
        }
        val handle = new ClassNodeHandle(bytes);
        if (TransformerUtil.executeTransformers(transformedName, handle, transformers)) {
            return handle.computeBytes();
        } else {
            return bytes;
        }
    }

    public static MergeableTurboTransformer merge(MergeableTurboTransformer a, MergeableTurboTransformer b) {
        val arr = new ArrayList<>(a.transformers);
        arr.addAll(b.transformers);
        return new MergeableTurboTransformer(arr);
    }

    public static void mergeAllTurboTransformers() {
        CoreLoadingPlugin.mergeTurboTransformers();
    }
}
