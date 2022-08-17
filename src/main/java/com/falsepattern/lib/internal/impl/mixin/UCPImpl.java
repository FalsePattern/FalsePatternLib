package com.falsepattern.lib.internal.impl.mixin;

import com.falsepattern.lib.mixin.MinecraftURLClassPath;
import lombok.val;

import net.minecraft.launchwrapper.LaunchClassLoader;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModClassLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;

public final class UCPImpl {
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

    public static void addJar(File pathToJar) throws Exception {
        if (!GRIMOIRE) {
            addURL.invoke(ucp, pathToJar.toURI().toURL());
        }
    }
}
