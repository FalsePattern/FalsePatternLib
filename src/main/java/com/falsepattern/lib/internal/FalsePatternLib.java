package com.falsepattern.lib.internal;

import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.lib.util.ResourceUtil;
import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.MetadataCollection;
import lombok.Getter;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class used by FalsePatternLib's internal code. This can change between versions without notice, so do not use this in your code!
 */
public class FalsePatternLib extends DummyModContainer {
    @Getter
    private static final Logger log = LogManager.getLogger(Tags.MODNAME);

    @Getter
    private static final boolean developerEnvironment = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    public FalsePatternLib() {
        super(MetadataCollection.from(ResourceUtil.getResourceFromJar("/mcmod.info", FalsePatternLib.class), Tags.MODID).getMetadataForId(Tags.MODID, null));
        log.info("Version " + Tags.VERSION + " initialized!");
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        ConfigurationManager.init();
        return true;
    }
}
