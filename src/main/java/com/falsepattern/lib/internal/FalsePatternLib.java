/*
 * This file is part of FalsePatternLib.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalsePatternLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * FalsePatternLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalsePatternLib. If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.lib.internal;

import com.falsepattern.lib.internal.asm.CoreLoadingPlugin;
import com.falsepattern.lib.internal.config.ConfigEngineConfig;
import com.falsepattern.lib.internal.proxy.CommonProxy;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

/**
 * Utility class used by FalsePatternLib's internal code. This can change between versions without notice, so do not use
 * this in your code!
 */
@Mod(modid = Tags.MODID,
     name = Tags.MODNAME,
     version = Tags.VERSION,
     acceptedMinecraftVersions = "[1.7.10]",
     guiFactory = Tags.GROUPNAME + ".internal.config.LibraryGuiFactory",
     acceptableRemoteVersions = "*")
public class FalsePatternLib {
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID);

    @SidedProxy(clientSide = Tags.GROUPNAME + ".internal.proxy.ClientProxy",
                serverSide = Tags.GROUPNAME + ".internal.proxy.CommonProxy")
    private static CommonProxy proxy;
    static {
        CoreLoadingPlugin.mergeTurboTransformers();
    }

    public FalsePatternLib() {
        FPLog.LOG.info("Version " + Tags.VERSION + " initialized!");
    }

    @Mod.EventHandler
    public void construct(FMLConstructionEvent e) {
        ConfigEngineConfig.poke();
        proxy.construct(e);
        Share.EARLY_INIT_DONE = true;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init(e);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
    }
}
