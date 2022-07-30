/**
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
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
package com.falsepattern.lib.internal;

import com.falsepattern.lib.internal.proxy.CommonProxy;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.event.FMLConstructionEvent;
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
     guiFactory = Tags.GROUPNAME + ".internal.config.LibraryGuiFactory",
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
