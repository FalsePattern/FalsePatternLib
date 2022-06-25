package com.falsepattern.lib.internal.proxy;

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.internal.LibraryConfig;
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
        ConfigurationManager.registerBus();
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
