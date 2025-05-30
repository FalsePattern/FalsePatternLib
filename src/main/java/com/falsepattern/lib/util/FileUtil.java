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
package com.falsepattern.lib.util;

import com.falsepattern.lib.StableAPI;
import lombok.experimental.UtilityClass;

import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
@StableAPI(since = "0.8.2")
public final class FileUtil {
    @StableAPI.Expose
    public static File getMinecraftHome() {
        return getMinecraftHomePath().toFile();
    }

    @StableAPI.Expose(since = "1.0.0")
    public static Path getMinecraftHomePath() {
        return Launch.minecraftHome == null ? Paths.get("").toAbsolutePath() : Launch.minecraftHome.toPath();
    }
}
