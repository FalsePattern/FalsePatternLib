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
import org.spongepowered.asm.launch.MixinBootstrap;

import java.util.Optional;

@StableAPI(since = "0.10.2")
public final class MixinInfo {
    @StableAPI.Expose
    public static final MixinBootstrapperType mixinBootstrapper = detect();

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

    private static MixinBootstrapperType detect() {
        try {
            Class.forName("org.spongepowered.asm.launch.MixinBootstrap");
        } catch (ClassNotFoundException ignored) {
            return MixinBootstrapperType.None;
        }
        try {
            Class.forName("ru.timeconqueror.spongemixins.core.SpongeMixinsCore");
            return MixinBootstrapperType.SpongeMixins;
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Class.forName("io.github.crucible.grimoire.Grimoire");
            return MixinBootstrapperType.Grimoire;
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Class.forName("io.github.crucible.grimoire.common.GrimoireCore");
            return MixinBootstrapperType.Grimoire;
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Class.forName("io.github.tox1cozz.mixinbooterlegacy.MixinBooterLegacyPlugin");
            return MixinBootstrapperType.MixinBooterLegacy;
        } catch (ClassNotFoundException ignored) {
        }

        return MixinBootstrapperType.Other;
    }

    @StableAPI(since = "0.10.2")
    public enum MixinBootstrapperType {
        @StableAPI.Expose None,
        @StableAPI.Expose SpongeMixins,
        @StableAPI.Expose Grimoire,
        @StableAPI.Expose MixinBooterLegacy,
        @StableAPI.Expose Other
    }
}
