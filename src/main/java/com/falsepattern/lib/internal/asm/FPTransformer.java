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

package com.falsepattern.lib.internal.asm;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.asm.transformers.ConfigOrderTransformer;
import com.falsepattern.lib.internal.asm.transformers.MixinPluginTransformer;
import com.falsepattern.lib.internal.asm.transformers.TypeDiscovererModuleInfoSilencer;
import com.falsepattern.lib.internal.impl.optifine.OptiFineTransformerHooksImpl;
import com.falsepattern.lib.turboasm.MergeableTurboTransformer;
import lombok.experimental.Accessors;

import java.util.Arrays;

@Accessors(fluent = true)
@StableAPI(since = "__INTERNAL__")
public class FPTransformer extends MergeableTurboTransformer {
    static {
        OptiFineTransformerHooksImpl.init();
    }

    public FPTransformer() {
        super(Arrays.asList(new MixinPluginTransformer(),
                            new TypeDiscovererModuleInfoSilencer(),
                            new ConfigOrderTransformer()));
    }
}
