package com.falsepattern.lib.internal;

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.lib.updates.ModUpdateInfo;
import com.falsepattern.lib.updates.UpdateChecker;
import com.falsepattern.lib.util.ResourceUtil;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.MetadataCollection;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Utility class used by FalsePatternLib's internal code. This can change between versions without notice, so do not use
 * this in your code!
 */
@SuppressWarnings("UnstableApiUsage")
public class FalsePatternLib extends DummyModContainer {
    @Getter private static final Logger log = LogManager.getLogger(Tags.MODNAME);

    @Getter private static final boolean developerEnvironment =
            (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    private Future<List<ModUpdateInfo>> updateCheckFuture;

    public FalsePatternLib() {
        super(MetadataCollection.from(ResourceUtil.getResourceFromJar("/mcmod.info", FalsePatternLib.class), Tags.MODID)
                                .getMetadataForId(Tags.MODID, null));
        log.info("Version " + Tags.VERSION + " initialized!");
    }

    @SuppressWarnings("UnstableApiUsage")
    @Subscribe
    public void construct(FMLConstructionEvent e) {
        ConfigurationManager.init();
        try {
            ConfigurationManager.registerConfig(LibraryConfig.class);
        } catch (ConfigException ex) {
            getLog().error("Failed to register " + Tags.MODNAME + " config!", ex);
        }
    }

    @Subscribe
    public void preInit(FMLPreInitializationEvent e) {
        if (LibraryConfig.ENABLE_UPDATE_CHECKER) {
            getLog().info("Launching asynchronous update check. I'll check back on it during postInit.");
            updateCheckFuture = UpdateChecker.fetchUpdatesAsync("https://falsepattern.com/mc/versions.json");
        }
    }

    @Subscribe
    public void postInit(FMLPostInitializationEvent e) {
        if (updateCheckFuture != null && !updateCheckFuture.isCancelled()) {
            try {
                val updates = updateCheckFuture.get();
                if (updates != null)
                    for (val update: updates)
                        update.log(getLog());
            } catch (InterruptedException | ExecutionException ex) {
                getLog().warn("Error while checking updates", ex);
            }

        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }
}
