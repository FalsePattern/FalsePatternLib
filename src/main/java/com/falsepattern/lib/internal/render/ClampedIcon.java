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

package com.falsepattern.lib.internal.render;

import com.falsepattern.lib.util.MathUtil;
import lombok.AllArgsConstructor;

import net.minecraft.util.IIcon;

@AllArgsConstructor
public final class ClampedIcon implements IIcon {
    private final IIcon delegate;

    @Override
    public int getIconWidth() {
        return delegate.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return delegate.getIconHeight();
    }

    @Override
    public float getMinU() {
        return delegate.getMinU();
    }

    @Override
    public float getMaxU() {
        return delegate.getMaxU();
    }

    @Override
    public float getMinV() {
        return delegate.getMinV();
    }

    @Override
    public float getMaxV() {
        return delegate.getMaxV();
    }

    @Override
    public float getInterpolatedU(double textureU) {
        return delegate.getInterpolatedU(clampTextureCoordinate(textureU));
    }

    @Override
    public float getInterpolatedV(double textureV) {
        return delegate.getInterpolatedV(clampTextureCoordinate(textureV));
    }

    @Override
    public String getIconName() {
        return delegate.getIconName();
    }

    private double clampTextureCoordinate(double textureCoordinate) {
        return MathUtil.clamp(textureCoordinate, 0D, 16D);
    }
}
