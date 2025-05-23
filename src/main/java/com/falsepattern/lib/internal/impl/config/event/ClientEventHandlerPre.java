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

package com.falsepattern.lib.internal.impl.config.event;

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.event.ConfigSyncEvent;
import com.falsepattern.lib.config.event.ConfigValidationFailureEvent;
import com.falsepattern.lib.internal.FPLog;
import com.falsepattern.lib.internal.config.ConfigEngineConfig;
import com.falsepattern.lib.text.FormattedText;
import com.falsepattern.lib.toasts.GuiToast;
import com.falsepattern.lib.toasts.SimpleToast;
import com.falsepattern.lib.toasts.icon.ToastBG;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import net.minecraft.util.EnumChatFormatting;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SideOnly(Side.CLIENT)
public class ClientEventHandlerPre {
    private static final ClientEventHandlerPre INSTANCE = new ClientEventHandlerPre();

    public static void registerBus() {
        FMLCommonHandler.instance().bus().register(INSTANCE);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onValidationErrorToast(ConfigValidationFailureEvent e) {
        if (ConfigEngineConfig.CONFIG_ERROR_LOGGING == ConfigEngineConfig.LoggingLevel.LogAndToast) {
            e.toast();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onConfigSyncFinished(ConfigSyncEvent.End e) {
        val cfg = e.configClass.getAnnotation(Config.class);
        if (e.successful) {
            if (ConfigEngineConfig.CONFIG_SYNC_SUCCESS_LOGGING == ConfigEngineConfig.LoggingLevel.LogAndToast) {
                GuiToast.add(new SimpleToast(ToastBG.TOAST_DARK,
                                             null,
                                             FormattedText.parse(EnumChatFormatting.GREEN + "Synced config")
                                                          .toChatText()
                                                          .get(0),
                                             FormattedText.parse(cfg.modid() + ":" + cfg.category()).toChatText().get(0),
                                             false,
                                             5000));
            }
            if (ConfigEngineConfig.CONFIG_SYNC_SUCCESS_LOGGING != ConfigEngineConfig.LoggingLevel.None) {
                FPLog.LOG.info("Synced config: {}:{}", cfg.modid(), cfg.category());
            }
        } else {
            if (ConfigEngineConfig.CONFIG_SYNC_FAILURE_LOGGING == ConfigEngineConfig.LoggingLevel.LogAndToast) {
                GuiToast.add(new SimpleToast(ToastBG.TOAST_DARK,
                                             null,
                                             FormattedText.parse(EnumChatFormatting.RED + "Failed to sync config")
                                                          .toChatText()
                                                          .get(0),
                                             FormattedText.parse(cfg.modid() + ":" + cfg.category()).toChatText().get(0),
                                             false,
                                             5000));
            }
            if (ConfigEngineConfig.CONFIG_SYNC_FAILURE_LOGGING != ConfigEngineConfig.LoggingLevel.None) {
                FPLog.LOG.error("Failed to sync config: {}:{}", cfg.modid(), cfg.category());
                val t = e.error;
                if (t != null) {
                    FPLog.LOG.error(t.getMessage(), t);
                }
            }

        }
    }
}
