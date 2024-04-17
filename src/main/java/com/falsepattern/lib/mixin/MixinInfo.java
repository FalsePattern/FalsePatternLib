/*
 * This file is part of FalsePatternLib.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalsePatternLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalsePatternLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalsePatternLib. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lib.mixin;

import com.falsepattern.lib.StableAPI;
import lombok.val;
import org.spongepowered.asm.launch.MixinBootstrap;

import net.minecraft.launchwrapper.Launch;

import java.io.IOException;
import java.util.Optional;

@StableAPI(since = "0.10.2")
public final class MixinInfo {
    @StableAPI.Expose
    public static final MixinBootstrapperType mixinBootstrapper;

    static {
        MixinInfoCompatCompanion.mixinInfoClassLoaded = true;
        mixinBootstrapper = detect();
    }

    @StableAPI.Expose
    public static boolean isMixinsInstalled() {
        return mixinBootstrapper != MixinBootstrapperType.None;
    }

    @StableAPI.Expose
    public static boolean isOfficialSpongeMixins() {
        return (mixinBootstrapper == MixinBootstrapperType.SpongeMixins) && getVerUnsafe().equals("0.7.11");
    }

    /**
     * The nh fork of spongemixins breaks some stuff since 1.5.0.
     */
    @StableAPI.Expose
    public static boolean isTamperedSpongeMixins() {
        return (mixinBootstrapper == MixinBootstrapperType.SpongeMixins) && getVerUnsafe().equals("0.7.12");
    }

    @StableAPI.Expose
    public static boolean isGrimoire() {
        return mixinBootstrapper == MixinBootstrapperType.Grimoire;
    }

    @StableAPI.Expose
    public static boolean isMixinBooterLegacy() {
        return mixinBootstrapper == MixinBootstrapperType.MixinBooterLegacy;
    }

    @StableAPI.Expose(since = "0.10.14")
    public static boolean isGasStation() {
        return mixinBootstrapper == MixinBootstrapperType.GasStation;
    }

    @StableAPI.Expose(since = "0.10.15")
    public static boolean isUniMixin() {
        return mixinBootstrapper == MixinBootstrapperType.UniMixin;
    }

    @StableAPI.Expose
    public static MixinBootstrapperType bootstrapperType() {
        return mixinBootstrapper;
    }

    @StableAPI.Expose
    public static Optional<String> mixinVersion() {
        return mixinBootstrapper != MixinBootstrapperType.None ? Optional.of(getVerUnsafe()) : Optional.empty();
    }

    private static String getVerUnsafe() {
        return MixinBootstrap.VERSION;
    }

    public static boolean isClassPresentSafe(String clazz) {
        try {
            val bytes = Launch.classLoader.getClassBytes(clazz);
            if (bytes == null || bytes.length == 0) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private static MixinBootstrapperType detect() {
        if (!isClassPresentSafe("org.spongepowered.asm.launch.MixinBootstrap")) {
            return MixinBootstrapperType.None;
        }
        for (val candidate : MixinInfoCompatCompanion.UNIMIXIN_CANDIDATES) {
            if (isClassPresentSafe(candidate)) {
                return MixinBootstrapperType.UniMixin;
            }
        }
        if (isClassPresentSafe("com.falsepattern.gasstation.GasStation")) {
            return MixinBootstrapperType.GasStation;
        }
        if (isClassPresentSafe("ru.timeconqueror.spongemixins.core.SpongeMixinsCore")) {
            return MixinBootstrapperType.SpongeMixins;
        }
        if (isClassPresentSafe("io.github.crucible.grimoire.Grimoire") || isClassPresentSafe(
                "io.github.crucible.grimoire.common.GrimoireCore")) {
            return MixinBootstrapperType.Grimoire;
        }
        if (isClassPresentSafe("io.github.tox1cozz.mixinbooterlegacy.MixinBooterLegacyPlugin")) {
            return MixinBootstrapperType.MixinBooterLegacy;
        }
        return MixinBootstrapperType.Other;
    }

    @StableAPI(since = "0.10.2")
    public enum MixinBootstrapperType {
        @StableAPI.Expose None,
        @StableAPI.Expose(since = "0.10.9") GasStation,
        @StableAPI.Expose SpongeMixins,
        @StableAPI.Expose Grimoire,
        @StableAPI.Expose MixinBooterLegacy,
        @StableAPI.Expose Other,
        @StableAPI.Expose(since = "0.10.15") UniMixin
    }
}
