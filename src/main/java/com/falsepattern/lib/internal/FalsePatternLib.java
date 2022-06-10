package com.falsepattern.lib.internal;

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.lib.internal.proxy.CommonProxy;
import com.falsepattern.lib.text.FormattedText;
import com.falsepattern.lib.updates.ModUpdateInfo;
import com.falsepattern.lib.updates.UpdateChecker;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.Getter;
import lombok.val;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.event.ClickEvent;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class used by FalsePatternLib's internal code. This can change between versions without notice, so do not use
 * this in your code!
 */
@SuppressWarnings("UnstableApiUsage")
@Mod(modid = Tags.MODID,
     name = Tags.MODNAME,
     version = Tags.VERSION,
     acceptedMinecraftVersions = "[1.7.10]",
     acceptableRemoteVersions = "*")
public class FalsePatternLib {
    @Getter private static final Logger log = LogManager.getLogger(Tags.MODNAME);

    @Getter private static final boolean developerEnvironment =
            (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    @SidedProxy(clientSide = Tags.GROUPNAME + ".internal.proxy.ClientProxy", serverSide = Tags.GROUPNAME + ".internal.proxy.CommonProxy")
    private static CommonProxy proxy;

    public FalsePatternLib() {
        log.info("Version " + Tags.VERSION + " initialized!");
    }

    @Mod.EventHandler
    public void construct(FMLConstructionEvent e) {
        proxy.construct(e);
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
