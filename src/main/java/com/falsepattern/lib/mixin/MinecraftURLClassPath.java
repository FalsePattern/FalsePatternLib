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
package com.falsepattern.lib.mixin;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.impl.mixin.UCPImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;

/**
 * Backport from spongemixins 1.3 for compat with the curseforge 1.2.0 version.
 * <p>
 * Also added Grimoire protection.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@StableAPI(since = "0.10.0")
public final class MinecraftURLClassPath {
    @StableAPI.Expose
    public static void addJar(File pathToJar) throws Exception {
        UCPImpl.addJar(pathToJar);
    }
}
