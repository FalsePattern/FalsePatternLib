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
import lombok.val;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@StableAPI(since = "0.8.0")
public interface IMixin {

    String getMixin();

    default boolean shouldLoad(List<ITargetedMod> loadedMods) {
        val side = getSide();
        return (side == Side.COMMON || side == Side.SERVER && FMLLaunchHandler.side().isServer() ||
                side == Side.CLIENT && FMLLaunchHandler.side().isClient()) && getFilter().test(loadedMods);
    }

    Side getSide();

    Predicate<List<ITargetedMod>> getFilter();

    enum Side {
        COMMON,
        CLIENT,
        SERVER
    }

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
