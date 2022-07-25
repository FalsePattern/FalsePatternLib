package com.falsepattern.lib.internal;

import com.falsepattern.lib.internal.proxy.CommonProxy;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import lombok.Getter;

import net.minecraft.launchwrapper.Launch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class used by FalsePatternLib's internal code. This can change between versions without notice, so do not use
 * this in your code!
 */
@Mod(modid = Tags.MODID,
     name = Tags.MODNAME,
     version = Tags.VERSION,
     acceptedMinecraftVersions = "[1.7.10]",
     guiFactory = Tags.GROUPNAME + ".internal.LibraryGuiFactory",
     acceptableRemoteVersions = "*")
public class FalsePatternLib {
    public static final String UPDATE_URL = "https://falsepattern.com/mc/versions.json";

    @Getter private static final Logger log = LogManager.getLogger(Tags.MODNAME);

    @Getter private static final boolean developerEnvironment =
            (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    @SidedProxy(clientSide = Tags.GROUPNAME + ".internal.proxy.ClientProxy", serverSide = Tags.GROUPNAME + ".internal.proxy.CommonProxy")
    private static CommonProxy proxy;

    public FalsePatternLib() {
        log.info("Version " + Tags.VERSION + " initialized!");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
    }

}
