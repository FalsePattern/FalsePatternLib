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

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.lib.internal.Tags;

@Config.Comment("Toast notification settings")
@Config.LangKey
@Config(modid = Tags.MODID,
        category = "toasts")
public class ToastConfig {
    static {
        ConfigurationManager.selfInit();
    }

    @Config.Comment("The maximum amount of toasts to show on the screen.")
    @Config.LangKey
    @Config.DefaultInt(5)
    @Config.RangeInt(min = 1,
                     max = 10)
    @Config.Name(value = "maxVisible",
                 migrations = "")
    public static int MAX_VISIBLE;

    @Config.Comment("The amount of empty space from the top of the screen in pixels.")
    @Config.LangKey
    @Config.DefaultInt(0)
    @Config.RangeInt(min = 0,
                     max = 10000)
    @Config.Name(value = "yOffset",
                 migrations = "")
    public static int Y_OFFSET;

    @Config.Comment("Which side of the screen should toasts show up on.")
    @Config.LangKey
    @Config.DefaultEnum("Right")
    @Config.Name(value = "align",
                 migrations = "")
    public static Side ALIGN;

    public static boolean leftAlign() {
        return ALIGN == Side.Left;
    }

    public enum Side {
        @StableAPI.Expose(since = "__INTERNAL__") Left,
        @StableAPI.Expose(since = "__INTERNAL__") Right
    }
}
