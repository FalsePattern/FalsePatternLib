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
import com.falsepattern.lib.internal.Tags;
import lombok.NonNull;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

@StableAPI(since = "0.10.0")
public class ToastBG extends ToastTexture {
    @StableAPI(since = "0.10.0")
    public static final ToastBG TOAST_DARK = new ToastBG(new ResourceLocation(Tags.MODID, "textures/gui/toast_dark.png"), 160, 32, 0xFFFFFFFF);
    @StableAPI(since = "0.10.0")
    public static final ToastBG TOAST_DARK_ROUNDED = new ToastBG(new ResourceLocation(Tags.MODID, "textures/gui/toast_dark_rounded.png"), 160, 32, 0xFFFFFFFF);
    @StableAPI(since = "0.10.0")
    public static final ToastBG TOAST_NARRATOR = new ToastBG(new ResourceLocation(Tags.MODID, "textures/gui/toast_narrator.png"), 160, 32, 0xFFAAAA00);
    @StableAPI(since = "0.10.0")
    public static final ToastBG TOAST_NARRATOR_ROUNDED = new ToastBG(new ResourceLocation(Tags.MODID, "textures/gui/toast_narrator_rounded.png"), 160, 32, 0xFFAAAA00);
    @StableAPI(since = "0.10.0")
    public static final ToastBG TOAST_WHITE = new ToastBG(new ResourceLocation(Tags.MODID, "textures/gui/toast_white.png"), 160, 32, 0xFF500050);
    @StableAPI(since = "0.10.0")
    public static final ToastBG TOAST_WHITE_ROUNDED = new ToastBG(new ResourceLocation(Tags.MODID, "textures/gui/toast_white_rounded.png"), 160, 32, 0xFF500050);

    @StableAPI(since = "0.10.0")
    public final int defaultTextColor;

    @StableAPI(since = "0.10.0")
    public ToastBG(@NonNull ResourceLocation texture, int width, int height, int defaultTextColor) {
        super(texture, width, height);
        this.defaultTextColor = defaultTextColor;
    }

    @StableAPI(since = "0.10.0")
    public ToastBG(@NonNull ResourceLocation texture, @NonNull IIcon icon, int defaultTextColor) {
        super(texture, icon);
        this.defaultTextColor = defaultTextColor;
    }

    @Override
    public void draw(Gui guiIn, int x, int y) {
        GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        guiIn.drawTexturedModelRectFromIcon(x, y, icon, icon.getIconWidth(), icon.getIconHeight());
        GL11.glPopAttrib();
    }
}