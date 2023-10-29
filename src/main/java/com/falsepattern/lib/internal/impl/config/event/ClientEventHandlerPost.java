/*
 * Copyright (C) 2022-2023 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
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

package com.falsepattern.lib.internal.impl.config.event;

import com.falsepattern.lib.config.event.ConfigSyncRequestEvent;
import com.falsepattern.lib.internal.impl.config.ConfigurationManagerImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.concurrent.atomic.AtomicBoolean;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SideOnly(Side.CLIENT)
public final class ClientEventHandlerPost {
    private static final ClientEventHandlerPost INSTANCE = new ClientEventHandlerPost();

    public static void registerBus() {
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        FMLCommonHandler.instance().bus().register(INSTANCE);
    }

    @SideOnly(Side.CLIENT)
    @SneakyThrows
    @SubscribeEvent
    public void onSyncRequestClient(ConfigSyncRequestEvent.Client e) {
        ConfigurationManagerImpl.sendSyncRequest();
    }

    private AtomicBoolean shouldDoConfigSync = new AtomicBoolean(false);

    @SneakyThrows
    @SubscribeEvent
    public void onJoinWorld(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        if (e.isLocal)
            return;
        shouldDoConfigSync.set(true);
    }

    @SneakyThrows
    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent e) {
        if (e.world.isRemote && e.entity instanceof EntityClientPlayerMP && shouldDoConfigSync.compareAndSet(true, false)) {
            ConfigurationManagerImpl.sendSyncRequest();
        }
    }
}
