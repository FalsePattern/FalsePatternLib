/*
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
package com.falsepattern.lib.mixin;

import com.falsepattern.lib.StableAPI;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import net.minecraft.launchwrapper.LaunchClassLoader;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModClassLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * Backport from spongemixins 1.3 for compat with the curseforge 1.2.0 version.
 * <p>
 * Also added Grimoire protection.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@StableAPI(since = "0.10.0")
public final class MinecraftURLClassPath {
    /**
     * Utility to manipulate the minecraft URL ClassPath
     */

    private static final Object ucp;
    private static final Method addURL;
    private static final boolean GRIMOIRE;

    static {
        boolean grimoire = false;
        String[] knownGrimoireClassNames =
                new String[]{"io.github.crucible.grimoire.Grimoire", "io.github.crucible.grimoire.common.GrimoireCore"};
        for (val className : knownGrimoireClassNames) {
            try {
                Class.forName(className, false, MinecraftURLClassPath.class.getClassLoader());
                grimoire = true;
                break;
            } catch (ClassNotFoundException ignored) {
            }
        }
        GRIMOIRE = grimoire;
        if (!GRIMOIRE) {
            try {
                val modClassLoaderField = Loader.class.getDeclaredField("modClassLoader");
                modClassLoaderField.setAccessible(true);

                val loaderInstanceField = Loader.class.getDeclaredField("instance");
                loaderInstanceField.setAccessible(true);

                val mainClassLoaderField = ModClassLoader.class.getDeclaredField("mainClassLoader");
                mainClassLoaderField.setAccessible(true);

                val ucpField = LaunchClassLoader.class.getSuperclass().getDeclaredField("ucp");
                ucpField.setAccessible(true);

                Object loader = loaderInstanceField.get(null);
                val modClassLoader = (ModClassLoader) modClassLoaderField.get(loader);
                val mainClassLoader = (LaunchClassLoader) mainClassLoaderField.get(modClassLoader);
                ucp = ucpField.get(mainClassLoader);
                addURL = ucp.getClass().getDeclaredMethod("addURL", URL.class);
            } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e.getMessage());
            }
        } else {
            ucp = null;
            addURL = null;
            System.err.println("Grimoire detected, disabling jar loading utility");
        }
    }

    /**
     * Adds a Jar to the Minecraft URL ClassPath - Needed when using mixins on classes outside of Minecraft or other
     * coremods
     */
    @StableAPI.Expose
    public static void addJar(File pathToJar) throws Exception {
        if (!GRIMOIRE) {
            addURL.invoke(ucp, pathToJar.toURI().toURL());
        }
    }
}
