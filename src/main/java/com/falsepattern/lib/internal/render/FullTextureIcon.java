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

package com.falsepattern.lib.internal.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.IIcon;

@Getter
@AllArgsConstructor
public final class FullTextureIcon implements IIcon {
    private final String iconName;

    private final int iconWidth;
    private final int iconHeight;

    @Override
    public float getMinU() {
        return 0F;
    }

    @Override
    public float getMinV() {
        return 0F;
    }

    @Override
    public float getMaxU() {
        return 1F;
    }

    @Override
    public float getMaxV() {
        return 1F;
    }

    @Override
    public float getInterpolatedU(double textureU) {
        return (float) textureU / 16F;
    }

    @Override
    public float getInterpolatedV(double textureV) {
        return (float) textureV / 16F;
    }
}
