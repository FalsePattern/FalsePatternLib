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
package com.falsepattern.lib.compat;

import com.falsepattern.lib.StableAPI;
import lombok.NonNull;
import lombok.val;

import net.minecraft.util.EnumFacing;

@StableAPI(since = "0.6.0")
public enum Rotation {
    @StableAPI.Expose NONE,
    @StableAPI.Expose CLOCKWISE_90,
    @StableAPI.Expose CLOCKWISE_180,
    @StableAPI.Expose COUNTERCLOCKWISE_90;

    @StableAPI.Expose
    public Rotation add(@NonNull Rotation rotation) {
        val values = Rotation.values();
        return values[(ordinal() + rotation.ordinal()) % values.length];
    }

    @StableAPI.Expose
    public EnumFacing rotate(@NonNull EnumFacing facing) {
        if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
            return facing;
        }
        switch (this) {
            case CLOCKWISE_90:
                switch (facing) {
                    case NORTH:
                        return EnumFacing.EAST;
                    case EAST:
                        return EnumFacing.SOUTH;
                    case SOUTH:
                        return EnumFacing.WEST;
                    case WEST:
                        return EnumFacing.NORTH;
                    default:
                        throw new IllegalStateException("Unable to get Y-rotated facing of " + facing);
                }
            case CLOCKWISE_180:
                switch (facing) {
                    case NORTH:
                        return EnumFacing.SOUTH;
                    case EAST:
                        return EnumFacing.WEST;
                    case SOUTH:
                        return EnumFacing.NORTH;
                    case WEST:
                        return EnumFacing.EAST;
                    default:
                        throw new IllegalStateException("Unable to get Y-rotated facing of " + facing);
                }
            case COUNTERCLOCKWISE_90:
                switch (facing) {
                    case NORTH:
                        return EnumFacing.WEST;
                    case EAST:
                        return EnumFacing.NORTH;
                    case SOUTH:
                        return EnumFacing.EAST;
                    case WEST:
                        return EnumFacing.SOUTH;
                    default:
                        throw new IllegalStateException("Unable to get Y-rotated facing of " + facing);
                }
            default:
                return facing;
        }
    }

    @StableAPI.Expose
    public int rotate(int x, int z) {
        switch (this) {
            case CLOCKWISE_90:
                return (x + z / 4) % z;
            case CLOCKWISE_180:
                return (x + z / 2) % z;
            case COUNTERCLOCKWISE_90:
                return (x + z * 3 / 4) % z;
            default:
                return x;
        }
    }
}