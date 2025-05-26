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

import cpw.mods.fml.relauncher.FMLLaunchHandler;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface IMixin {
    String getMixin();
    Side getSide();
    Predicate<List<ITargetedMod>> getFilter();

    default boolean shouldLoad(List<ITargetedMod> loadedMods) {
        val side = getSide();
        return (side == Side.COMMON
                || side == Side.SERVER && FMLLaunchHandler.side().isServer()
                || side == Side.CLIENT && FMLLaunchHandler.side().isClient()) && getFilter().test(loadedMods);
    }

    /**
     * @since 0.10.0
     */
    enum Side {
        COMMON,
        CLIENT,
        SERVER
    }

    /**
     * @since 0.10.0
     */
    final class PredicateHelpers {
        public static Predicate<List<ITargetedMod>> never() {
            return (list) -> false;
        }

        public static Predicate<List<ITargetedMod>> condition(Supplier<Boolean> condition) {
            return (list) -> condition.get();
        }

        public static Predicate<List<ITargetedMod>> always() {
            return (list) -> true;
        }

        public static Predicate<List<ITargetedMod>> require(ITargetedMod mod) {
            return (list) -> list.contains(mod);
        }

        public static Predicate<List<ITargetedMod>> avoid(ITargetedMod mod) {
            return (list) -> !list.contains(mod);
        }
    }
}
