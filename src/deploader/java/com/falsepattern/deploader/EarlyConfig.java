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

package com.falsepattern.deploader;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.val;
import lombok.var;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

//For stuff that runs before FML does
@Data
@Accessors(fluent = true)
public class EarlyConfig {
    @Expose
    private boolean enableLibraryDownloads;

    @Expose
    private boolean enableLetsEncryptRoot;

    private static volatile EarlyConfig instance = null;

    private static final Logger LOG = LogManager.getLogger("FalsePatternLib Deploader Early Config");

    public static @NotNull EarlyConfig getInstance() {
        val config = instance;
        if (config != null)
            return config;
        return loadFromDisk();
    }

    private static synchronized @NotNull EarlyConfig loadFromDisk() {
        var config = instance;
        if (config != null) {
            return config;
        }
        val configDir = LowLevelCallMultiplexer.gameDir().resolve("config");
        val configFile = configDir.resolve("falsepatternlib-early.json");
        val gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            if (Files.exists(configFile)) {
                config = gson.fromJson(new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8),
                                       EarlyConfig.class);
            }
        } catch (IOException e) {
            LOG.error("Failed to load from disk", e);
        }
        if (config == null) {
            config = new EarlyConfig();
            config.enableLibraryDownloads(true);
            config.enableLetsEncryptRoot(true);
            try {
                Files.write(configFile, gson.toJson(config).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                LOG.error("Failed to write to disk", e);
            }
        }
        instance = config;
        return config;
    }
}
