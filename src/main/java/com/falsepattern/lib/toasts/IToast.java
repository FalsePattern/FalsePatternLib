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
package com.falsepattern.lib.toasts;

import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.impl.toast.GuiToastImpl;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IToast {
    Object NO_TOKEN = new Object();

    Visibility draw(GuiToastImpl toastGui, long delta);

    int width();

    int height();

    default Object getType() {
        return NO_TOKEN;
    }

    @SideOnly(Side.CLIENT)
    enum Visibility {
        SHOW(new ResourceLocation(Tags.MODID, "ui.toast.in")),
        HIDE(new ResourceLocation(Tags.MODID, "ui.toast.out"));

        private final ResourceLocation sound;

        Visibility(ResourceLocation soundIn) {
            this.sound = soundIn;
        }

        public void playSound(SoundHandler handler) {
            handler.playSound(PositionedSoundRecord.func_147674_a(this.sound, 1.0F));
        }
    }
}