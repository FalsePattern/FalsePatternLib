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

package com.falsepattern.lib.internal.config;

import com.falsepattern.lib.internal.FPLog;
import lombok.NoArgsConstructor;
import lombok.val;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.GuiIngameModOptions;
import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class InGameModOptionsFix {
    private static boolean isInitialized = false;

    private static Field MODS_FIELD;

    public static void init() {
        if (isInitialized)
            return;

        try {
            MODS_FIELD = GuiModList.class.getDeclaredField("mods");
            MODS_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            MODS_FIELD = null;
            FPLog.LOG.error("Failed to get field: cpw.mods.fml.client.GuiModList.mods,"
                            + " In-Game Mod Options Fix will not work", e);
            return;
        }
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
            event.gui = new GuiModConfigList(Minecraft.getMinecraft().currentScreen);
    }

    private static class GuiModConfigList extends GuiModList {
        private GuiModConfigList(GuiScreen screen) {
            super(screen);
            val mods = new ArrayList<ModContainer>();
            for (val mod : getModsFromPrivateField()) {
                val guiFactory = FMLClientHandler.instance().getGuiFactoryFor(mod);
                if (guiFactory == null)
                    continue;
                if (guiFactory.mainConfigGuiClass() != null)
                    mods.add(mod);
            }
            setModsToPrivateField(mods);
        }

        private List<ModContainer> getModsFromPrivateField() {
            try {
                return (List<ModContainer>) MODS_FIELD.get(this);
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }

        private void setModsToPrivateField(List<ModContainer> mods) {
            try {
                MODS_FIELD.set(this, mods);
            } catch (Exception ignored) {
            }
        }
    }
}
