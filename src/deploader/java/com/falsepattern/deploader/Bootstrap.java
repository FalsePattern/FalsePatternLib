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

package com.falsepattern.deploader;

import com.gtnewhorizons.retrofuturabootstrap.RfbApiImpl;
import lombok.val;

import net.minecraft.launchwrapper.Launch;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;

import java.net.URLClassLoader;
import java.nio.file.Path;

public class Bootstrap {
    public static Path MINECRAFT_HOME_PATH;

    public static void bootstrap(boolean rfb, Path homePath) {
        MINECRAFT_HOME_PATH = homePath;
        if (rfb) {
            val loader = RfbApiImpl.INSTANCE.launchClassLoader();
            PreShare.initDevState(((URLClassLoader)loader).findResource("net/minecraft/world/World.class") != null);
            PreShare.initClientState(((URLClassLoader)loader).findResource("net/minecraft/client/Minecraft.class") != null ||
                                     ((URLClassLoader)loader).findResource("bao.class") != null);
            LowLevelCallMultiplexer.rfbDetected();
        } else {
            PreShare.initDevState(Launch.classLoader.findResource("net/minecraft/world/World.class") != null);
            PreShare.initClientState(FMLLaunchHandler.side() == Side.CLIENT);
        }
        LetsEncryptHelper.replaceSSLContext();
    }

    public static void runDepLoader() {
        DependencyLoaderImpl.executeDependencyLoading();
    }
}
