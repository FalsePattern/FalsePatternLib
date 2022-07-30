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
import javax.annotation.concurrent.Immutable;

import com.falsepattern.lib.util.MathUtil;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * A functional equivalent to Vec3i present in Minecraft 1.12.
 */
@Getter
@Immutable
@EqualsAndHashCode
@AllArgsConstructor
@StableAPI(since = "0.6.0")
public class Vec3i implements Comparable<Vec3i> {
    /**
     * A static zero vector
     */
    public static final Vec3i NULL_VECTOR = new Vec3i(0, 0, 0);

    protected final int x;
    protected final int y;
    protected final int z;

    /**
     * Instantiates a new vector.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     */
    public Vec3i(double x, double y, double z) {
        this(MathUtil.floor(x), MathUtil.floor(y), MathUtil.floor(z));
    }

    public int compareTo(@NonNull Vec3i vec) {
        return y == vec.getY() ? z == vec.getZ() ? x - vec.getX() : z - vec.getZ() : y - vec.getY();
    }

    /**
     * Cross product between two vectors.
     *
     * @param vec the other vector
     *
     * @return the new resulting vector
     */
    public Vec3i crossProduct(@NonNull Vec3i vec) {
        return new Vec3i(y * vec.getZ() - z * vec.getY(),
                         z * vec.getX() - x * vec.getZ(),
                         x * vec.getY() - y * vec.getX());
    }

    /**
     * Gets distance to the vector.
     *
     * @param x the other x
     * @param y the other y
     * @param z the other z
     *
     * @return the distance
     */
    public double getDistance(int x, int y, int z) {
        return Math.sqrt(distanceSq(x, y, z));
    }

    /**
     * Square root distance to a point.
     *
     * @param x the other x
     * @param y the other y
     * @param z the other z
     *
     * @return the square distance
     */
    public double distanceSq(int x, int y, int z) {
        val dX = this.x - x;
        val dY = this.y - y;
        val dZ = this.z - z;
        return dX * dX + dY * dY + dZ * dZ;
    }

    /**
     * Square distance between vectors.
     *
     * @param vec the other vector
     *
     * @return the square distance
     */
    public double distanceSq(@NonNull Vec3i vec) {
        return distanceSq(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Square distance between point to center of this Block.
     *
     * @param x the other x
     * @param y the other y
     * @param z the other z
     *
     * @return the square distance to center
     */
    public double distanceSqToCenter(double x, double y, double z) {
        val dX = this.x + 0.5D - x;
        val dY = this.y + 0.5D - y;
        val dZ = this.z + 0.5D - z;
        return dX * dX + dY * dY + dZ * dZ;
    }
}