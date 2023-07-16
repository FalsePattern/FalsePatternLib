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

package com.falsepattern.lib.internal.asm;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.asm.IClassNodeTransformer;
import com.falsepattern.lib.asm.SmartTransformer;
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.asm.transformers.ConfigOrderTransformer;
import com.falsepattern.lib.internal.asm.transformers.GasStationValidatorTransformer;
import com.falsepattern.lib.internal.asm.transformers.IMixinPluginTransformer;
import com.falsepattern.lib.internal.asm.transformers.ITypeDiscovererTransformer;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

@Accessors(fluent = true)
@StableAPI(since = "__INTERNAL__")
public class FPTransformer implements SmartTransformer {
    public static final Logger LOG = LogManager.getLogger(Tags.MODNAME + " ASM");

    @Getter
    private final List<IClassNodeTransformer> transformers;

    @Getter
    private final Logger logger = LOG;

    public FPTransformer() {
        transformers = Arrays.asList(new IMixinPluginTransformer(),
                                     new ITypeDiscovererTransformer(),
                                     new GasStationValidatorTransformer(),
                                     new ConfigOrderTransformer());
    }
}
