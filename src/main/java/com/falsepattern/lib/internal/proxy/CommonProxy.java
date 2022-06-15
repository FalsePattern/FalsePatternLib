package com.falsepattern.lib.internal.proxy;

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.internal.LibraryConfig;
import com.falsepattern.lib.updates.ModUpdateInfo;
import com.falsepattern.lib.updates.UpdateChecker;
import com.falsepattern.lib.util.AsyncUtil;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import lombok.val;
import lombok.var;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CommonProxy {

    protected Future<List<ModUpdateInfo>> updatesFuture;

    public void preInit(FMLPreInitializationEvent e) {
        try {
            ConfigurationManager.registerConfig(LibraryConfig.class);
        } catch (ConfigException ex) {
            FalsePatternLib.getLog().error("Failed to register config!", ex);
        }
        if (LibraryConfig.ENABLE_UPDATE_CHECKER) {
            FalsePatternLib.getLog().info("Launching asynchronous update check.");
            val updateCheckFuture = UpdateChecker.fetchUpdatesAsync(FalsePatternLib.UPDATE_URL);
            updatesFuture = AsyncUtil.asyncWorker.submit(new Callable<List<ModUpdateInfo>>() {
                @Override
                public List<ModUpdateInfo> call() {
                    //Deadlock avoidance
                    if (updateCheckFuture.isCancelled()) {
                        updatesFuture = null;
                        return null;
                    }
                    if (!updateCheckFuture.isDone()) {
                        updatesFuture = AsyncUtil.asyncWorker.submit(this);
                        return null;
                    }
                    try {
                        var updates = updateCheckFuture.get();
                        if (updates != null && updates.size() > 0) {
                            for (val update : updates) {
                                update.log(FalsePatternLib.getLog());
                            }
                        } else if (updates == null) {
                            FalsePatternLib.getLog().warn("Unknown error while checking updates.");
                        } else {
                            FalsePatternLib.getLog().info("All checked mods up to date!");
                            updates = null;
                        }
                        return updates;
                    } catch (InterruptedException | ExecutionException ex) {
                        FalsePatternLib.getLog().warn("Error while checking updates", ex);
                    }
                    return null;
                }
            });
        }
    }

    public void postInit(FMLPostInitializationEvent e) {

    }


}
