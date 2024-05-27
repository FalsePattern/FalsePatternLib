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
package com.falsepattern.lib.asm;

import com.falsepattern.lib.DeprecationDetails;
import com.falsepattern.lib.StableAPI;
import org.objectweb.asm.tree.ClassNode;

/**
 * See: {@link com.falsepattern.lib.turboasm.TurboClassTransformer}.
 * This class will not be removed, for backwards compatibility reasons.
 */
@StableAPI(since = "0.10.0")
@Deprecated
@DeprecationDetails(deprecatedSince = "1.2.0")
public interface IClassNodeTransformer {
    @StableAPI.Expose
    String getName();

    @StableAPI.Expose
    boolean shouldTransform(ClassNode cn, String transformedName, boolean obfuscated);

    @StableAPI.Expose
    default int internalSortingOrder() {
        return 0;
    }

    @StableAPI.Expose
    void transform(ClassNode cn, String transformedName, boolean obfuscated);
}
