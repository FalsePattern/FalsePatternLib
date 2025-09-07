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

package com.falsepattern.lib.mixin.v2;

import com.gtnewhorizon.gtnhmixins.builders.IBaseTransformer;
import com.gtnewhorizon.gtnhmixins.builders.ITargetMod;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class MixinHelper {
    public static @NotNull MixinBuilder builder(@NotNull SidedMixins @NotNull ... mixins) {
        return builderImpl(null, null, mixins);
    }

    public static @NotNull MixinBuilder builder(@Nullable BooleanSupplier cond, @NotNull SidedMixins @NotNull ... mixins) {
        return builderImpl(cond, null, mixins);
    }

    public static @NotNull MixinBuilder builder(@NotNull TaggedMod mod, @NotNull SidedMixins @NotNull ... mixins) {
        return builderImpl(null, mods(mod), mixins);
    }

    public static @NotNull MixinBuilder builder(@NotNull TaggedMod @Nullable [] mods, @NotNull SidedMixins @NotNull ... mixins) {
        return builderImpl(null, mods, mixins);
    }

    public static @NotNull MixinBuilder builder(@NotNull BooleanSupplier cond, @NotNull TaggedMod mod, @NotNull SidedMixins @NotNull ... mixins) {
        return builderImpl(cond, mods(mod), mixins);
    }

    public static @NotNull MixinBuilder builder(@NotNull BooleanSupplier cond, @NotNull TaggedMod @NotNull [] mods, @NotNull SidedMixins @NotNull ... mixins) {
        return builderImpl(cond, mods, mixins);
    }

    private static @NotNull MixinBuilder builderImpl(@Nullable BooleanSupplier cond, @NotNull TaggedMod @Nullable [] requiredMods, @NotNull SidedMixins @NotNull ... mixins) {
        val builder = new MixinBuilder();
        if (requiredMods != null) {
            for (val mod : requiredMods) {
                if (mod.require) {
                    builder.addRequiredMod(mod.mod);
                } else {
                    builder.addExcludedMod(mod.mod);
                }
            }
        }
        for (val mixin : mixins) {
            builder.addSidedMixins(mixin.side, mixin.mixins);
        }
        if (cond != null) {
            builder.setApplyIf(cond::getAsBoolean);
        }
        return builder;
    }

    public static @NotNull SidedMixins common(@NotNull String @NotNull ... mixins) {
        for (int i = 0; i < mixins.length; i++) {
            mixins[i] = "common." + mixins[i];
        }
        return new SidedMixins(IBaseTransformer.Side.COMMON, mixins);
    }

    public static @NotNull SidedMixins client(@NotNull String @NotNull ... mixins) {
        for (int i = 0; i < mixins.length; i++) {
            mixins[i] = "client." + mixins[i];
        }
        return new SidedMixins(IBaseTransformer.Side.CLIENT, mixins);
    }

    public static @NotNull SidedMixins server(@NotNull String @NotNull ... mixins) {
        for (int i = 0; i < mixins.length; i++) {
            mixins[i] = "server." + mixins[i];
        }
        return new SidedMixins(IBaseTransformer.Side.SERVER, mixins);
    }

    public static @NotNull TaggedMod require(@NotNull ITargetMod mod) {
        return new TaggedMod(true, mod);
    }

    public static @NotNull TaggedMod avoid(@NotNull ITargetMod mod) {
        return new TaggedMod(false, mod);
    }

    public static @NotNull TaggedMod @NotNull [] mods(@NotNull TaggedMod @NotNull ... t) {
        return t;
    }

}
