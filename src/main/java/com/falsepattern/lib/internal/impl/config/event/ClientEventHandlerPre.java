/*
 * Copyright (C) 2022-2023 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
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

package com.falsepattern.lib.internal.impl.config.event;

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.event.ConfigSyncEvent;
import com.falsepattern.lib.config.event.ConfigValidationFailureEvent;
import com.falsepattern.lib.internal.config.LibraryConfig;
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
        if (LibraryConfig.CONFIG_ERROR_LOUDNESS == LibraryConfig.ValidationLogging.LogAndToast) {
            e.toast();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onConfigSyncFinished(ConfigSyncEvent.End e) {
        val cfg = e.configClass.getAnnotation(Config.class);
        if (e.successful) {
            GuiToast.add(new SimpleToast(ToastBG.TOAST_DARK,
                                         null,
                                         FormattedText.parse(EnumChatFormatting.GREEN + "Synced config")
                                                      .toChatText()
                                                      .get(0),
                                         FormattedText.parse(cfg.modid() + ":" + cfg.category()).toChatText().get(0),
                                         false,
                                         5000));
        } else {
            GuiToast.add(new SimpleToast(ToastBG.TOAST_DARK,
                                         null,
                                         FormattedText.parse(EnumChatFormatting.RED + "Failed to sync config")
                                                      .toChatText()
                                                      .get(0),
                                         FormattedText.parse(cfg.modid() + ":" + cfg.category()).toChatText().get(0),
                                         false,
                                         5000));
        }
    }
}
