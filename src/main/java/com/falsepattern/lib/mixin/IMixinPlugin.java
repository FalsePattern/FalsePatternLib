package com.falsepattern.lib.mixin;

import com.falsepattern.lib.internal.CoreLoadingPlugin;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.val;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import static java.nio.file.Files.walk;

public interface IMixinPlugin extends IMixinConfigPlugin {
    Path MODS_DIRECTORY_PATH = CoreLoadingPlugin.mcDir.toPath().resolve("mods");

    static Logger createLogger(String modName) {
        return LogManager.getLogger(modName + " Mixin Loader");
    }

    @Override
    default void onLoad(String mixinPackage) {

    }

    @Override
    default String getRefMapperConfig() {
        return null;
    }

    @Override
    default boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    default void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    default List<String> getMixins() {
        val isDevelopmentEnvironment = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        val targetedMods = getTargetedModEnumValues();
        val loadedMods = Arrays.stream(targetedMods)
                               .filter(mod -> (mod.isLoadInDevelopment() && isDevelopmentEnvironment) || loadJarOf(mod))
                               .collect(Collectors.toList());

        for (val mod : targetedMods) {
            if (loadedMods.contains(mod)) {
                getLogger().info("Found " + mod.getModName() + "! Integrating now...");
            } else {
                getLogger().info("Could not find " + mod.getModName() + "! Skipping integration....");
            }
        }

        List<String> mixins = new ArrayList<>();
        for (val mixin : getMixinEnumValues()) {
            if (mixin.shouldLoad(loadedMods)) {
                String mixinClass = mixin.getSide().name().toLowerCase() + "." + mixin.getMixin();
                mixins.add(mixinClass);
                getLogger().info("Loading mixin: " + mixinClass);
            }
        }
        return mixins;
    }

    ITargetedMod[] getTargetedModEnumValues();

    default boolean loadJarOf(final ITargetedMod mod) {
        boolean success = false;
        try {
            File jar = findJarOf(mod);
            if (jar == null) {
                getLogger().info("Jar not found for " + mod);
                return false;
            }
            getLogger().info("Attempting to add " + jar + " to the URL Class Path");
            success = true;
            if (!jar.exists()) {
                success = false;
                throw new FileNotFoundException(jar.toString());
            }
            MinecraftURLClassPath.addJar(jar);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return success;
    }

    Logger getLogger();

    IMixin[] getMixinEnumValues();

    static File findJarOf(final ITargetedMod mod) {
        try {
            return walk(MODS_DIRECTORY_PATH).filter(mod::isMatchingJar).map(Path::toFile).findFirst().orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    default void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    default void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
