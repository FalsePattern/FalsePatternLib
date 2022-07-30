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
import com.falsepattern.lib.util.RenderUtil;
import lombok.Getter;
import lombok.NonNull;

import net.minecraft.client.gui.Gui;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

@StableAPI(since = "0.10.0")
public abstract class ToastTexture {
    @Getter(onMethod_={@StableAPI(since = "0.10.0")})
    protected final IIcon icon;
    @Getter(onMethod_={@StableAPI(since = "0.10.0")})
    protected final ResourceLocation texture;

    @StableAPI(since = "0.10.0")
    public ToastTexture(@NonNull ResourceLocation texture, int width, int height) {
        this(texture, RenderUtil.getFullTextureIcon(texture.toString(), width, height));
    }

    @StableAPI(since = "0.10.0")
    public ToastTexture(@NonNull ResourceLocation texture, @NonNull IIcon icon) {
        this.icon = icon;
        this.texture = texture;
    }

    /**
     * Draws the icon at the specified position in the specified Gui
     */
    @StableAPI(since = "0.10.0")
    public abstract void draw(Gui guiIn, int x, int y);
}