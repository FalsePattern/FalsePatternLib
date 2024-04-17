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

package com.falsepattern.lib.internal.config;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.core.LowLevelCallMultiplexer;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.val;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

//For stuff that runs before FML does
@Data
@Accessors(fluent = true)
public class EarlyConfig {
    @Expose
    @StableAPI.Expose(since = "__INTERNAL__")
    private boolean enableLibraryDownloads;

    private static EarlyConfig instance = null;

    @SneakyThrows
    public static EarlyConfig load() {
        if (instance != null)
            return instance;
        val configFile = LowLevelCallMultiplexer.gameDir().resolve("config").resolve("falsepatternlib-early.json");
        val gson = new Gson();
        EarlyConfig config;
        if (!Files.exists(configFile)) {
            config = new EarlyConfig();
            config.enableLibraryDownloads(true);
        } else {
            config = gson.fromJson(new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8), EarlyConfig.class);
        }
        Files.write(configFile, gson.toJson(config).getBytes(StandardCharsets.UTF_8));
        instance = config;
        return config;
    }
}
