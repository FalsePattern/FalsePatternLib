/**
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
package com.falsepattern.lib.internal.config;

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.internal.Tags;

@Config(modid = Tags.MODID,
        category = "toasts")
public class ToastConfig {
    @Config.Comment("The maximum amount of toasts to show on the screen")
    @Config.LangKey("config.falsepatternlib.maxtoasts")
    @Config.DefaultInt(5)
    @Config.RangeInt(min = 1,
                     max = 10)
    public static int MAX_VISIBLE;

    @Config.Comment("The amount of empty space from the top of the screen in pixels")
    @Config.LangKey("config.falsepatternlib.toastoffset")
    @Config.DefaultInt(0)
    @Config.RangeInt(min = 0,
                     max = 10000)
    public static int Y_OFFSET;

    @Config.Comment("Which side of the screen should toasts show up on")
    @Config.LangKey("config.falsepatternlib.toastalign")
    @Config.DefaultEnum("Right")
    public static Side ALIGN;

    public static boolean leftAlign() {
        return ALIGN == Side.Left;
    }

    public enum Side {
        Left, Right
    }
}
