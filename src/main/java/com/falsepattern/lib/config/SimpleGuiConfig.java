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
package com.falsepattern.lib.config;

import com.falsepattern.lib.StableAPI;

import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@StableAPI(since = "0.9.0")
public class SimpleGuiConfig extends GuiConfig {
    @StableAPI.Expose
    public SimpleGuiConfig(GuiScreen parent, Class<?> configClass, String modID, String modName)
            throws ConfigException {
        this(parent, modID, modName, configClass);
    }

    @StableAPI.Expose(since = "0.10.0")
    public SimpleGuiConfig(GuiScreen parent, String modID, String title, Class<?>... configClasses)
            throws ConfigException {
        this(parent, modID, title, null, configClasses);
    }

    @StableAPI.Expose(since = "1.5.0")
    public SimpleGuiConfig(GuiScreen parent, String modID, String title, String titleLine2, Class<?>... configClasses)
            throws ConfigException {
        super(parent,
              ConfigurationManager.getConfigElementsMultiStructured(configClasses),
              modID,
              false,
              false,
              title,
              titleLine2);
    }
}
