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
package com.falsepattern.lib.config;

import com.falsepattern.lib.StableAPI;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
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

    @StableAPI(since = "0.10.0")
    public SimpleGuiConfig(GuiScreen parent, String modID, String modName, Class<?>... configClasses)
            throws ConfigException {
        super(parent, ConfigurationManager.getConfigElementsMulti(configClasses), modID, false, false,
              modName + " Configuration", I18n.format("falsepatternlib.gui.config.description"));
    }
}
