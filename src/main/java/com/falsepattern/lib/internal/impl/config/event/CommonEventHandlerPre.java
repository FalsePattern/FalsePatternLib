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

package com.falsepattern.lib.internal.impl.config.event;

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.event.ConfigSyncEvent;
import com.falsepattern.lib.config.event.ConfigValidationFailureEvent;
import com.falsepattern.lib.internal.FPLog;
import com.falsepattern.lib.internal.config.ConfigEngineConfig;
import com.falsepattern.lib.internal.config.MiscConfig;
import com.falsepattern.lib.internal.impl.config.ConfigurationManagerImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonEventHandlerPre {
    private static final CommonEventHandlerPre INSTANCE = new CommonEventHandlerPre();

    public static void registerBus() {
        FMLCommonHandler.instance().bus().register(INSTANCE);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        ConfigurationManagerImpl.onConfigChanged(event);
    }

    @SubscribeEvent
    public void onValidationErrorLog(ConfigValidationFailureEvent e) {
        if (ConfigEngineConfig.CONFIG_ERROR_LOGGING != ConfigEngineConfig.LoggingLevel.None) {
            e.logWarn();
        }
    }

    @SubscribeEvent
    public void onConfigSyncFinished(ConfigSyncEvent.End e) {
            if (e.successful) {
                if (ConfigEngineConfig.CONFIG_SYNC_SUCCESS_LOGGING != ConfigEngineConfig.LoggingLevel.None) {
                    val cfg = e.configClass.getAnnotation(Config.class);
                    FPLog.LOG.info("Synced config: {}:{}", cfg.modid(), cfg.category());
                }
            } else {
                if (ConfigEngineConfig.CONFIG_SYNC_FAILURE_LOGGING != ConfigEngineConfig.LoggingLevel.None) {
                    val cfg = e.configClass.getAnnotation(Config.class);
                    FPLog.LOG.error("Failed to sync config: {}:{}", cfg.modid(), cfg.category());
                    val t = e.error;
                    if (t != null) {
                        FPLog.LOG.error(t.getMessage(), t);
                    }
            }
        }
    }
}
