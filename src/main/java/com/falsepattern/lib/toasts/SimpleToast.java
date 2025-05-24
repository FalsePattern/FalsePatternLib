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

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.impl.toast.GuiToastImpl;
import com.falsepattern.lib.toasts.icon.ToastBG;
import com.falsepattern.lib.toasts.icon.ToastIcon;
import com.falsepattern.lib.util.MathUtil;
import lombok.NonNull;
import lombok.val;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.Gui;
import net.minecraft.util.IChatComponent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
@StableAPI(since = "0.10.0")
public class SimpleToast implements IToast {
    private final ToastBG background;
    private final ToastIcon icon;
    private final String title;
    private final String subtitle;
    private final boolean hasProgressBar;
    private final long timeout;
    private IToast.Visibility visibility = IToast.Visibility.SHOW;
    private long lastDelta;
    private float displayedProgress;
    private float currentProgress;

    @StableAPI.Expose
    public SimpleToast(@NonNull ToastBG background,
                       @Nullable ToastIcon icon, IChatComponent titleComponent,
                       @Nullable IChatComponent subtitleComponent, boolean drawProgressBar, long timeout) {
        this.background = background;
        this.icon = icon;
        this.title = titleComponent.getFormattedText();
        this.subtitle = subtitleComponent == null ? null : subtitleComponent.getFormattedText();
        this.hasProgressBar = drawProgressBar;
        this.timeout = timeout;
    }

    @Override
    public IToast.Visibility draw(GuiToastImpl toastGui, long delta) {
        GL11.glColor3f(1, 1, 1);
        background.draw(toastGui, 0, 0);
        if (icon != null) {
            icon.draw(toastGui, 6, 6);
        }

        val x = icon == null ? 5 : 5 + icon.getIcon().getIconWidth();
        val bgHeight = background.getIcon().getIconHeight();
        val fontRenderer = toastGui.getMinecraft().fontRenderer;
        if (subtitle == null) {
            fontRenderer.drawString(title, x, bgHeight / 2 - 5, 0xFFFFFFFF);
        } else {
            fontRenderer.drawString(title, x, 7, 0xFFFFFFFF);
            fontRenderer.drawString(subtitle, x, bgHeight - 14, 0xFFFFFFFF);
        }

        if (hasProgressBar) {
            val bgWidth = background.getIcon().getIconWidth();
            Gui.drawRect(3, bgHeight - 4, bgWidth - 3, bgHeight - 3, 0xFFFFFFFF);
            float f = (float) MathUtil.clampedLerp(displayedProgress,
                                                   currentProgress,
                                                   (float) (delta - lastDelta) / 100.0F);
            int i;

            if (currentProgress >= displayedProgress) {
                i = 0xff005500;
            } else {
                i = 0xff550000;
            }
            val barLength = bgWidth - 6;
            Gui.drawRect(3, bgHeight - 4, (int) (3.0F + barLength * f), bgHeight - 3, i);
            displayedProgress = f;
            lastDelta = delta;
        }
        if (timeout > 0) {
            if (delta >= timeout) {
                hide();
            }
        }
        return visibility;
    }

    @Override
    public int width() {
        return background.getIcon().getIconWidth();
    }

    @Override
    public int height() {
        return background.getIcon().getIconHeight();
    }

    @StableAPI.Expose
    public void hide() {
        visibility = IToast.Visibility.HIDE;
    }

    @StableAPI.Expose
    public void setProgress(float progress) {
        this.currentProgress = progress;
    }
}