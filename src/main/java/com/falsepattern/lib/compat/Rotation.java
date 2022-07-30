/**
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
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
package com.falsepattern.lib.compat;

import com.falsepattern.lib.StableAPI;
import lombok.NonNull;
import net.minecraft.util.EnumFacing;

@StableAPI(since = "0.6.0")
public enum Rotation {
    NONE,
    CLOCKWISE_90,
    CLOCKWISE_180,
    COUNTERCLOCKWISE_90;

    public Rotation add(@NonNull Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180:
                switch (this) {
                    case NONE:
                        return CLOCKWISE_180;
                    case CLOCKWISE_90:
                        return COUNTERCLOCKWISE_90;
                    case CLOCKWISE_180:
                        return NONE;
                    case COUNTERCLOCKWISE_90:
                        return CLOCKWISE_90;
                }
            case COUNTERCLOCKWISE_90:
                switch (this) {
                    case NONE:
                        return COUNTERCLOCKWISE_90;
                    case CLOCKWISE_90:
                        return NONE;
                    case CLOCKWISE_180:
                        return CLOCKWISE_90;
                    case COUNTERCLOCKWISE_90:
                        return CLOCKWISE_180;
                }
            case CLOCKWISE_90:
                switch (this) {
                    case NONE:
                        return CLOCKWISE_90;
                    case CLOCKWISE_90:
                        return CLOCKWISE_180;
                    case CLOCKWISE_180:
                        return COUNTERCLOCKWISE_90;
                    case COUNTERCLOCKWISE_90:
                        return NONE;
                }
            default:
                return this;
        }
    }

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