package com.falsepattern.lib.mixin;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModClassLoader;
import lombok.val;
import net.minecraft.launchwrapper.LaunchClassLoader;
import sun.misc.URLClassPath;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Backport from spongemixins 1.3 for compat with the curseforge 1.2.0 version.
 *
 * Also added Grimoire protection.
 */
public final class MinecraftURLClassPath {
    /**
     *  Utility to manipulate the minecraft URL ClassPath
     */

    private static final URLClassPath ucp;
    private static final boolean GRIMOIRE;

    static {
        boolean grimoire;
        try {
            Class.forName("io.github.crucible.grimoire.Grimoire", false, MinecraftURLClassPath.class.getClassLoader());
            grimoire = true;
        } catch (ClassNotFoundException ignored) {
            grimoire = false;
        }
        GRIMOIRE = grimoire;
        if (!GRIMOIRE) {
            try {
                val modClassLoaderField = Loader.class.getDeclaredField("modClassLoader");
                modClassLoaderField.setAccessible(true);

                val loaderinstanceField = Loader.class.getDeclaredField("instance");
                loaderinstanceField.setAccessible(true);

                val mainClassLoaderField = ModClassLoader.class.getDeclaredField("mainClassLoader");
                mainClassLoaderField.setAccessible(true);

                val ucpField = LaunchClassLoader.class.getSuperclass().getDeclaredField("ucp");
                ucpField.setAccessible(true);

                Object loader = loaderinstanceField.get(null);
                val modClassLoader = (ModClassLoader) modClassLoaderField.get(loader);
                val mainClassLoader = (LaunchClassLoader) mainClassLoaderField.get(modClassLoader);
                ucp = (URLClassPath) ucpField.get(mainClassLoader);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e.getMessage());
            }
        } else {
            ucp = null;
            System.err.println("Grimoire detected, disabling jar loading utility");
        }
    }

    /**
     * Adds a Jar to the Minecraft URL ClassPath
     *  - Needed when using mixins on classes outside of Minecraft or other coremods
     */
    public static void addJar(File pathToJar) throws Exception {
        if (!GRIMOIRE)
            ucp.addURL(pathToJar.toURI().toURL());
    }

    private MinecraftURLClassPath() {
    }


}
