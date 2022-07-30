/**
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
package com.falsepattern.lib.internal.proxy;

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.internal.LibraryConfig;
import com.falsepattern.lib.internal.impl.config.ConfigurationManagerImpl;
import com.falsepattern.lib.updates.ModUpdateInfo;
import com.falsepattern.lib.updates.UpdateChecker;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import lombok.val;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommonProxy {

    protected CompletableFuture<List<ModUpdateInfo>> updatesFuture;

    public void preInit(FMLPreInitializationEvent e) {
        ConfigurationManagerImpl.registerBus();
        try {
            ConfigurationManager.registerConfig(LibraryConfig.class);
        } catch (ConfigException ex) {
            FalsePatternLib.getLog().error("Failed to register config!", ex);
        }
        if (LibraryConfig.ENABLE_UPDATE_CHECKER) {
            FalsePatternLib.getLog().info("Launching asynchronous update check.");
            updatesFuture = UpdateChecker.fetchUpdatesAsync(FalsePatternLib.UPDATE_URL).thenApplyAsync(updates -> {
                if (updates == null) {
                    updates = Collections.emptyList();
                }
                if (updates.isEmpty()) {
                    FalsePatternLib.getLog().info("No updates found.");
                } else {
                    FalsePatternLib.getLog().info("Found {} updates.", updates.size());
                    for (val update : updates) {
                        update.log(FalsePatternLib.getLog());
                    }
                }
                return updates;
            }).exceptionally(ex -> {
                FalsePatternLib.getLog().error("Failed to check for updates!", ex);
                return Collections.emptyList();
            });
        } else {
            updatesFuture = CompletableFuture.completedFuture(Collections.emptyList());
        }
    }

    public void postInit(FMLPostInitializationEvent e) {

    }


}
