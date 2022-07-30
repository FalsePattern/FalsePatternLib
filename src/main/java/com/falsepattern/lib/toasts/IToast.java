/**
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 * <p>
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.lib.toasts;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.impl.toast.GuiToastImpl;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@StableAPI(since = "0.10.0")
public interface IToast {
    @StableAPI(since = "0.10.0")
    Object NO_TOKEN = new Object();

    @StableAPI(since = "0.10.0")
    Visibility draw(GuiToastImpl toastGui, long delta);

    @StableAPI(since = "0.10.0")
    int width();

    @StableAPI(since = "0.10.0")
    int height();

    @StableAPI(since = "0.10.0")
    default Object getType() {
        return NO_TOKEN;
    }

    @StableAPI(since = "0.10.0")
    @SideOnly(Side.CLIENT)
    enum Visibility {
        SHOW(new ResourceLocation(Tags.MODID, "ui.toast.in")),
        HIDE(new ResourceLocation(Tags.MODID, "ui.toast.out"));

        private final ResourceLocation sound;

        Visibility(ResourceLocation soundIn) {
            this.sound = soundIn;
        }

        @StableAPI(since = "0.10.0")
        public void playSound(SoundHandler handler) {
            handler.playSound(PositionedSoundRecord.func_147674_a(this.sound, 1.0F));
        }
    }
}