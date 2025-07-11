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

package com.falsepattern.lib.mixin;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a class you can use to interact with MixinInfo before it gets classloaded.
 * (Originally placed here for future unimixins compat in case they change the class names)
 *
 * @since 0.10.15
 */
public class MixinInfoCompatCompanion {
    /**
     * A list of all mixin classes that are candidates for unimixins.
     * This is used to determine if a mixin plugin is unimixins. Once MixinInfo is classloaded, this list has no effect.
     */
    public static final List<String> UNIMIXIN_CANDIDATES = new ArrayList<>(Arrays.asList(
            "io.github.legacymoddingmc.unimixins.compat.CompatCore",
            "io.github.legacymoddingmc.unimixins.devcompat.DevCompatCore",
            "io.github.legacymoddingmc.unimixins.all.AllCore",
            "io.github.legacymoddingmc.unimixins.mixin.MixinModule"));

    @Getter
    static boolean mixinInfoClassLoaded = false;
}
