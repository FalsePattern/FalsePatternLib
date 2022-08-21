package com.falsepattern.lib.internal.impl.config;

import com.falsepattern.lib.config.event.ConfigSyncRequestEvent;
import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.internal.impl.config.net.SyncPrompt;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigSyncEventHandler {
    private static ConfigSyncEventHandler instance;

    public static void registerBus() {
        if (instance == null) {
            instance = new ConfigSyncEventHandler();
            MinecraftForge.EVENT_BUS.register(instance);
            FMLCommonHandler.instance().bus().register(instance);
        }
    }

    @SneakyThrows
    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent e) {
        if (e.world.isRemote && e.entity instanceof EntityClientPlayerMP) {
            ConfigurationManagerImpl.sendSyncRequest();
        }
    }

    @SideOnly(Side.CLIENT)
    @SneakyThrows
    @SubscribeEvent
    public void onSyncRequestClient(ConfigSyncRequestEvent.Client e) {
        ConfigurationManagerImpl.sendSyncRequest();
    }

    @SubscribeEvent
    public void onSyncRequestServer(ConfigSyncRequestEvent.Server e) {
        val players = e.getPlayers();
        if (players.size() == 0) {
            FalsePatternLib.NETWORK.sendToAll(new SyncPrompt());
        } else {
            for (EntityPlayerMP player : e.getPlayers()) {
                FalsePatternLib.NETWORK.sendTo(new SyncPrompt(), player);
            }
        }
    }
}
