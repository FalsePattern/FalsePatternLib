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

package com.falsepattern.lib.mixin;

import lombok.val;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.launch.MixinBootstrap;

import net.minecraft.launchwrapper.Launch;

import java.io.IOException;
import java.util.Optional;

/**
 * @since 0.10.2
 */
public final class MixinInfo {
    public static final MixinBootstrapperType mixinBootstrapper;

    static {
        MixinInfoCompatCompanion.mixinInfoClassLoaded = true;
        mixinBootstrapper = detect();
    }

    public static boolean isMixinsInstalled() {
        return mixinBootstrapper != MixinBootstrapperType.None;
    }

    public static boolean isOfficialSpongeMixins() {
        return (mixinBootstrapper == MixinBootstrapperType.SpongeMixins) && getVerUnsafe().equals("0.7.11");
    }

    /**
     * The nh fork of spongemixins breaks some stuff since 1.5.0.
     */
    public static boolean isTamperedSpongeMixins() {
        return (mixinBootstrapper == MixinBootstrapperType.SpongeMixins) && getVerUnsafe().equals("0.7.12");
    }

    public static boolean isGrimoire() {
        return mixinBootstrapper == MixinBootstrapperType.Grimoire;
    }

    public static boolean isMixinBooterLegacy() {
        return mixinBootstrapper == MixinBootstrapperType.MixinBooterLegacy;
    }

    /**
     * @since 0.10.14
     */
    public static boolean isGasStation() {
        return mixinBootstrapper == MixinBootstrapperType.GasStation;
    }

    /**
     * @since 0.10.15
     */
    public static boolean isUniMixin() {
        return mixinBootstrapper == MixinBootstrapperType.UniMixin;
    }

    public static MixinBootstrapperType bootstrapperType() {
        return mixinBootstrapper;
    }

    public static Optional<String> mixinVersion() {
        return mixinBootstrapper != MixinBootstrapperType.None ? Optional.of(getVerUnsafe()) : Optional.empty();
    }

    private static String getVerUnsafe() {
        return MixinBootstrap.VERSION;
    }

    @ApiStatus.Internal
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

    /**
     * @since 0.10.2
     */
    public enum MixinBootstrapperType {
        None,
        /** @since 0.10.9 */
        GasStation,
        SpongeMixins,
        Grimoire,
        MixinBooterLegacy,
        Other,
        /** @since 0.10.15 */
        UniMixin
    }
}
