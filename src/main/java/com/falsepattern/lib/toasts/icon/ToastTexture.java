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
package com.falsepattern.lib.toasts.icon;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.util.RenderUtil;
import lombok.Getter;
import lombok.NonNull;

import net.minecraft.client.gui.Gui;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@StableAPI(since = "0.10.0")
public abstract class ToastTexture {
    @Getter(onMethod_ = @StableAPI.Expose)
    protected final IIcon icon;
    @Getter(onMethod_ = @StableAPI.Expose)
    protected final ResourceLocation texture;

    @StableAPI.Expose
    public ToastTexture(@NonNull ResourceLocation texture, int width, int height) {
        this(texture, RenderUtil.getFullTextureIcon(texture.toString(), width, height));
    }

    @StableAPI.Expose
    public ToastTexture(@NonNull ResourceLocation texture, @NonNull IIcon icon) {
        this.icon = icon;
        this.texture = texture;
    }

    /**
     * Draws the icon at the specified position in the specified Gui
     */
    @StableAPI.Expose
    public abstract void draw(Gui guiIn, int x, int y);
}