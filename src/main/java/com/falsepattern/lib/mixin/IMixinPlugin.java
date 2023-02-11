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
import com.falsepattern.lib.util.FileUtil;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.Files.walk;

@StableAPI(since = "0.8.0")
public interface IMixinPlugin extends IMixinConfigPlugin {
    @StableAPI.Expose
    Path MODS_DIRECTORY_PATH = FileUtil.getMinecraftHome().toPath().resolve("mods");

    @StableAPI.Expose
    static Logger createLogger(String modName) {
        return LogManager.getLogger(modName + " Mixin Loader");
    }

    @StableAPI.Expose
    static File findJarOf(final ITargetedMod mod) {
        File result = null;
        try (val stream = walk(MODS_DIRECTORY_PATH)) {
            result = stream.filter(mod::isMatchingJar)
                           .map(Path::toFile)
                           .findFirst()
                           .orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result == null) {
            try {
                result = Arrays.stream(Launch.classLoader.getURLs())
                               .map(URL::getPath)
                               .map(Paths::get)
                               .filter(mod::isMatchingJar)
                               .map(Path::toFile)
                               .findFirst()
                               .orElse(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @StableAPI.Expose
    Logger getLogger();

    @StableAPI.Expose
    IMixin[] getMixinEnumValues();

    @StableAPI.Expose
    ITargetedMod[] getTargetedModEnumValues();

    @Override
    @StableAPI.Expose(since = "__INTERNAL__")
    default void onLoad(String mixinPackage) {

    }

    @Override
    @StableAPI.Expose(since = "__INTERNAL__")
    default String getRefMapperConfig() {
        return null;
    }

    @Override
    @StableAPI.Expose(since = "__INTERNAL__")
    default boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    @StableAPI.Expose(since = "__INTERNAL__")
    default void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    @StableAPI.Expose(since = "__INTERNAL__")
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

    @StableAPI.Expose(since = "__INTERNAL__")
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

    @StableAPI.Expose(since = "__INTERNAL__")
    @Override
    default void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @StableAPI.Expose(since = "__INTERNAL__")
    @Override
    default void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
