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
    @Config.Comment({"Used to control whether FalsePatternLib should check for outdated mods.",
                     "If you're building a public modpack, you should turn this off so that your users don't " +
                     "get nagged about outdated mods."})
    @Config.LangKey("config.falsepatternlib.updatecheck")
    @Config.DefaultBoolean(true)
    public static boolean ENABLE_UPDATE_CHECKER;

    @Config.Comment({"Used to control whether FalsePatternLib should be allowed to use the internet.",
                     "If this is disabled, library downloads will be blocked.",
                     "Note that if a mod tries to download a library that is not downloaded yet, the game will crash."})
    @Config.LangKey("config.falsepatternlib.disableinternet")
    @Config.DefaultBoolean(true)
    public static boolean ENABLE_LIBRARY_DOWNLOADS;

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
