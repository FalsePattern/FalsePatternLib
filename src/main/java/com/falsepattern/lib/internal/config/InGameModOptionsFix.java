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

package com.falsepattern.lib.internal.config;

import lombok.NoArgsConstructor;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.GuiIngameModOptions;
import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class InGameModOptionsFix {
    private static boolean isInitialized = false;

    public static void init() {
        if (isInitialized)
            return;
        MinecraftForge.EVENT_BUS.register(new InGameModOptionsFix());
        isInitialized = true;
    }

    @SubscribeEvent
    public void onGuiOpen(final GuiOpenEvent event) {
        if (!isInitialized)
            return;
        if (!MiscConfig.IN_GAME_MOD_OPTIONS_FIX)
            return;

        if (event.gui instanceof GuiIngameModOptions)
            event.gui = new GuiModList(Minecraft.getMinecraft().currentScreen);
    }
}
