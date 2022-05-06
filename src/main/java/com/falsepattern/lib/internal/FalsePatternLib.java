package com.falsepattern.lib.internal;

import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import lombok.Getter;
import lombok.val;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * Utility class used by FalsePatternLib's internal code. This can change between versions without notice, so do not use this in your code!
 */
public class FalsePatternLib extends DummyModContainer {
    @Getter
    private static final Logger log = LogManager.getLogger(Tags.MODNAME);

    @Getter
    private static final boolean developerEnvironment = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    public FalsePatternLib() {
        super(new ModMetadata());
        log.info("All your libraries are belong to us!");
        val meta = getMetadata();
        meta.modId = Tags.MODID;
        meta.name = Tags.MODNAME;
        meta.version = Tags.VERSION;
        meta.url = Tags.URL;
        meta.credits = Tags.CREDITS;
        meta.authorList = Arrays.asList(Tags.AUTHORS);
        meta.description = Tags.DESCRIPTION;
        meta.useDependencyInformation = true;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
}
