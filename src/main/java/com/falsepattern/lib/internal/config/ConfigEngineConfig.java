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

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;
import com.falsepattern.lib.internal.Tags;

@Config.Comment("Settings for the FPLib config system (also used by other mods)")
@Config.LangKey
@Config(modid = Tags.MODID,
        category = "config_engine")
public class ConfigEngineConfig {

    @Config.Comment("How the config error logging should be set up.")
    @Config.LangKey
    @Config.DefaultEnum("Log")
    @Config.Name(value = "configErrorLogging")
    public static LoggingLevel CONFIG_ERROR_LOGGING;

    @Config.Comment("How successful config synchronizations should be logged.")
    @Config.LangKey
    @Config.DefaultEnum("Log")
    @Config.Name(value = "configSyncSuccessLogging")
    public static LoggingLevel CONFIG_SYNC_SUCCESS_LOGGING;

    @Config.Comment("How failed config synchronizations should be logged.")
    @Config.LangKey
    @Config.DefaultEnum("LogAndToast")
    @Config.Name(value = "configSyncFailureLogging")
    public static LoggingLevel CONFIG_SYNC_FAILURE_LOGGING;

    static {
        ConfigurationManager.selfInit();
    }

    public static void poke() {

    }

    public enum LoggingLevel {
        None,
        Log,
        LogAndToast
    }
}
