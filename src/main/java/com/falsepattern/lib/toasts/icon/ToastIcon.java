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
package com.falsepattern.lib.toasts.icon;

import com.falsepattern.lib.StableAPI;
import lombok.NonNull;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

@StableAPI(since = "0.10.0")
public class ToastIcon extends ToastTexture {
    @StableAPI(since = "0.10.0")
    public ToastIcon(@NonNull ResourceLocation texture, int width, int height) {
        super(texture, width, height);
    }

    @StableAPI(since = "0.10.0")
    public ToastIcon(@NonNull ResourceLocation texture, @NonNull IIcon icon) {
        super(texture, icon);
    }

    @Override
    public void draw(Gui guiIn, int x, int y) {
        GL11.glPushAttrib(GL11.GL_TEXTURE_BIT | GL11.GL_ENABLE_BIT);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GL11.glEnable(GL11.GL_BLEND);
        guiIn.drawTexturedModelRectFromIcon(x, y, icon, icon.getIconWidth(), icon.getIconHeight());
        GL11.glPopAttrib();
    }
}