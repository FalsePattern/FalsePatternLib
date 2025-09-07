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

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public interface IMixinPlugin<E extends Enum<E> & IMixins> extends IMixinConfigPlugin {
    static Logger createLogger(String modName) {
        return LogManager.getLogger(modName + " Mixin Loader");
    }

    Logger getLogger();

    Class<E> getMixinEnum();

    @Override
    default void onLoad(String mixinPackage) {}

    @Override
    default @Nullable String getRefMapperConfig() {
        return null;
    }

    @Override
    default boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    default void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    default List<String> getMixins() {
        val logger = getLogger();
        logger.info("Scanning mixins...");
        try {
            return IMixins.getMixins(getMixinEnum());
        } finally {
            logger.info("Mixins scanned.");
        }
    }

    @Override
    default void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {}

    @Override
    default void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {}
}
