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

@Config(modid = Tags.MODID)
public class LibraryConfig {
    static {
        ConfigurationManager.selfInit();
    }

    @Config.Comment({"Fixes the mod options menu in-game.",
                     "By default, the mod options when in already in a game will show \"Test1, Test2, DISABLED\" in bright red.",
                     "This replaces that interface with the one from the main menu."})
    @Config.LangKey("config.falsepatternlib.ingamemodoptionsfix")
    @Config.DefaultBoolean(true)
    public static boolean IN_GAME_MOD_OPTIONS_FIX;

    @Config.Comment("How \"loud\" the config error logging should be.")
    @Config.LangKey("config.falsepatternlib.configlogging")
    @Config.DefaultEnum("Log")
    public static ValidationLogging CONFIG_ERROR_LOUDNESS;

    public enum ValidationLogging {
        @StableAPI.Expose(since = "__INTERNAL__") None,
        @StableAPI.Expose(since = "__INTERNAL__") Log,
        @StableAPI.Expose(since = "__INTERNAL__") LogAndToast
    }
}
