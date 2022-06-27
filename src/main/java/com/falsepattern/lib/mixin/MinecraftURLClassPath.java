package com.falsepattern.lib.mixin;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModClassLoader;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import net.minecraft.launchwrapper.LaunchClassLoader;

/**
 * Backport from spongemixins 1.3 for compat with the curseforge 1.2.0 version.
 * <p>
 * Also added Grimoire protection.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MinecraftURLClassPath {
    /**
     * Utility to manipulate the minecraft URL ClassPath
     */

    private static final Object ucp;
    private static final Method addURL;
    private static final boolean GRIMOIRE;

    static {
        boolean grimoire = false;
        String[] knownGrimoireClassNames = new String[]{
                "io.github.crucible.grimoire.Grimoire",
                "io.github.crucible.grimoire.common.GrimoireCore"
        };
        for (val className: knownGrimoireClassNames) {
            try {
                Class.forName(className, false, MinecraftURLClassPath.class.getClassLoader());
                grimoire = true;
                break;
            } catch (ClassNotFoundException ignored) {}
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
    public static void addJar(File pathToJar) throws Exception {
        if (!GRIMOIRE) {
            addURL.invoke(ucp, pathToJar.toURI().toURL());
        }
    }
}
