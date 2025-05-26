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
package com.falsepattern.lib.toasts.icon;

import com.falsepattern.lib.internal.Tags;
import lombok.NonNull;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ToastBG extends ToastTexture {
    public static final ToastBG TOAST_DARK =
            new ToastBG(new ResourceLocation(Tags.MODID, "textures/gui/toast_dark.png"), 160, 32, 0xFFFFFFFF);
    public static final ToastBG TOAST_DARK_ROUNDED =
            new ToastBG(new ResourceLocation(Tags.MODID, "textures/gui/toast_dark_rounded.png"), 160, 32, 0xFFFFFFFF);
    public static final ToastBG TOAST_NARRATOR =
            new ToastBG(new ResourceLocation(Tags.MODID, "textures/gui/toast_narrator.png"), 160, 32, 0xFFAAAA00);
    public static final ToastBG TOAST_NARRATOR_ROUNDED =
            new ToastBG(new ResourceLocation(Tags.MODID, "textures/gui/toast_narrator_rounded.png"),
                        160,
                        32,
                        0xFFAAAA00);
    public static final ToastBG TOAST_WHITE =
            new ToastBG(new ResourceLocation(Tags.MODID, "textures/gui/toast_white.png"), 160, 32, 0xFF500050);
    public static final ToastBG TOAST_WHITE_ROUNDED =
            new ToastBG(new ResourceLocation(Tags.MODID, "textures/gui/toast_white_rounded.png"), 160, 32, 0xFF500050);

    public final int defaultTextColor;

    public ToastBG(@NonNull ResourceLocation texture, int width, int height, int defaultTextColor) {
        super(texture, width, height);
        this.defaultTextColor = defaultTextColor;
    }

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