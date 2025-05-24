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

package com.falsepattern.lib.optifine;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.impl.optifine.OptiFineTransformerHooksImpl;

/**
 * A utility for manipulating OptiFine's patches.
 * If you have a mod that injects code that would otherwise conflict with OptiFine, and you have provably tested that
 * removing certain OptiFine patches from the jar does NOT break anything, you may use this class to disable them
 * without having to do jar file edits.
 */
@StableAPI(since = "1.0.0")
public class OptiFineTransformerHooks {
    @StableAPI.Expose
    public static void disableOptiFinePatch(String patchName) {
        OptiFineTransformerHooksImpl.disableOptiFinePatch(patchName);
    }
}
