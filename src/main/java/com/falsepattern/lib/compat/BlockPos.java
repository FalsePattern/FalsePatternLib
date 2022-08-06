/*
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
import com.falsepattern.lib.util.MathUtil;
import com.google.common.collect.AbstractIterator;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.UP;
import static net.minecraft.util.EnumFacing.WEST;

@Immutable
@StableAPI(since = "0.6.0")
public class BlockPos extends Vec3i {
    /**
     * An immutable block pos with zero as all coordinates.
     */
    @StableAPI.Expose
    public static final BlockPos ORIGIN = new BlockPos(0, 0, 0);
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int NUM_X_BITS = 1 + MathUtil.log2(MathUtil.smallestEncompassingPowerOfTwo(30000000));
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final int Y_SHIFT = NUM_Z_BITS;
    private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;
    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;

    @StableAPI.Expose
    public BlockPos(@NonNull Entity source) {
        this(source.posX, source.posY, source.posZ);
    }

    @StableAPI.Expose
    public BlockPos(double x, double y, double z) {
        super(x, y, z);
    }

    @StableAPI.Expose
    public BlockPos(@NonNull Vec3 vec) {
        this(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    @StableAPI.Expose
    public BlockPos(@NonNull Vec3i source) {
        this(source.getX(), source.getY(), source.getZ());
    }

    @StableAPI.Expose
    public BlockPos(int x, int y, int z) {
        super(x, y, z);
    }

    /**
     * Create a BlockPos from a serialized long value (created by toLong)
     */
    @StableAPI.Expose
    public static BlockPos fromLong(long serialized) {
        return new BlockPos((int) (serialized << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS),
                            (int) (serialized << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS),
                            (int) (serialized << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS));
    }

    /**
     * Create an Iterable that returns all positions in the box specified by the given corners. There is no requirement
     * that one corner is greater than the other; individual coordinates will be swapped as needed.
     * <p>
     * In situations where it is usable, prefer
     * {@link #getAllInBoxMutable(BlockPos, BlockPos) instead as it has better performance (fewer allocations)
     * <p>
     * @param from One corner of the box
     * @param to   Another corner of the box
     * <p>
     * @see #getAllInBox(int, int, int, int, int, int)
     * @see #getAllInBoxMutable(BlockPos, BlockPos)
     */
    @StableAPI.Expose
    public static Iterable<BlockPos> getAllInBox(@NonNull BlockPos from, @NonNull BlockPos to) {
        return getAllInBox(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()),
                           Math.min(from.getZ(), to.getZ()), Math.max(from.getX(), to.getX()),
                           Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
    }

    /**
     * Create an Iterable that returns all positions in the box specified by the coordinates. <strong>Coordinates must
     * be in order</strong>; e.g. x0 <= x1.
     * <p>
     * In situations where it is usable, prefer
     * {@link #getAllInBoxMutable(BlockPos, BlockPos) instead as it has better performance (fewer allocations)
     * <p>
     * @param x0 The lower x coordinate
     * @param y0 The lower y coordinate
     * @param z0 The lower z coordinate
     * @param x1 The upper x coordinate
     * @param y1 The upper y coordinate
     * @param z1 The upper z coordinate
     * <p>
     * @see #getAllInBox(BlockPos, BlockPos)
     * @see #getAllInBoxMutable(BlockPos, BlockPos)
     */
    @StableAPI.Expose
    public static Iterable<BlockPos> getAllInBox(final int x0, final int y0, final int z0, final int x1, final int y1, final int z1) {
        return () -> new AbstractIterator<BlockPos>() {
            private boolean first = true;
            private int lastX;
            private int lastY;
            private int lastZ;

            @Override
            protected BlockPos computeNext() {
                if (first) {
                    first = false;
                    lastX = x0;
                    lastY = y0;
                    lastZ = z0;
                    return new BlockPos(x0, y0, z0);
                }
                if (lastX == x1 && lastY == y1 && lastZ == z1) {
                    return endOfData();
                }
                if (lastX < x1) {
                    lastX++;
                } else if (lastY < y1) {
                    lastX = x0;
                    lastY++;
                } else if (lastZ < z1) {
                    lastX = x0;
                    lastY = y0;
                    lastZ++;
                }
                return new BlockPos(lastX, lastY, lastZ);
            }
        };
    }

    /**
     * Creates an Iterable that returns all positions in the box specified by the given corners. There is no requirement
     * that one corner is greater than the other; individual coordinates will be swapped as needed.
     * <p>
     * This method uses {@link BlockPos.MutableBlockPos MutableBlockPos} instead of regular BlockPos, which grants
     * better performance. However, the resulting BlockPos instances can only be used inside the iteration loop (as
     * otherwise the value will change), unless {@link #toImmutable()} is called. This method is ideal for searching
     * large areas and only storing a few locations.
     *
     * @param from One corner of the box
     * @param to   Another corner of the box
     *
     * @see #getAllInBox(BlockPos, BlockPos)
     * @see #getAllInBox(int, int, int, int, int, int)
     * @see #getAllInBoxMutable(BlockPos, BlockPos)
     */
    @StableAPI.Expose
    public static Iterable<BlockPos.MutableBlockPos> getAllInBoxMutable(@NonNull BlockPos from, @NonNull BlockPos to) {
        return getAllInBoxMutable(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()),
                                  Math.min(from.getZ(), to.getZ()), Math.max(from.getX(), to.getX()),
                                  Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
    }

    /**
     * Creates an Iterable that returns all positions in the box specified by the given corners. <strong>Coordinates
     * must be in order</strong>; e.g. x0 <= x1.
     * <p>
     * This method uses {@link BlockPos.MutableBlockPos MutableBlockPos} instead of regular BlockPos, which grants
     * better performance. However, the resulting BlockPos instances can only be used inside the iteration loop (as
     * otherwise the value will change), unless {@link #toImmutable()} is called. This method is ideal for searching
     * large areas and only storing a few locations.
     *
     * @param x0 The lower x coordinate
     * @param y0 The lower y coordinate
     * @param z0 The lower z coordinate
     * @param x1 The upper x coordinate
     * @param y1 The upper y coordinate
     * @param z1 The upper z coordinate
     *
     * @see #getAllInBox(BlockPos, BlockPos)
     * @see #getAllInBox(int, int, int, int, int, int)
     * @see #getAllInBoxMutable(BlockPos, BlockPos)
     */
    @StableAPI.Expose
    public static Iterable<BlockPos.MutableBlockPos> getAllInBoxMutable(final int x0, final int y0, final int z0, final int x1, final int y1, final int z1) {
        return () -> new AbstractIterator<MutableBlockPos>() {
            private MutableBlockPos pos;

            @Override
            protected MutableBlockPos computeNext() {
                if (pos == null) {
                    pos = new MutableBlockPos(x0, y0, z0);
                    return pos;
                } else if (pos.x == x1 && pos.y == y1 && pos.z == z1) {
                    return endOfData();
                } else {
                    if (pos.x < x1) {
                        pos.x++;
                    } else if (pos.y < y1) {
                        pos.x = x0;
                        pos.y++;
                    } else if (pos.z < z1) {
                        pos.x = x0;
                        pos.y = y0;
                        pos.z++;
                    }
                    return pos;
                }
            }
        };
    }

    @StableAPI.Expose
    public long toLong() {
        return ((long) x & X_MASK) << X_SHIFT | ((long) y & Y_MASK) << Y_SHIFT | ((long) z & Z_MASK);
    }

    /**
     * Add the given coordinates to the coordinates of this BlockPos
     */
    @StableAPI.Expose
    public BlockPos add(double x, double y, double z) {
        if (x == 0.0D && y == 0.0D && z == 0.0D) {
            return this;
        }
        return new BlockPos(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Add the given Vector to this BlockPos
     */
    @StableAPI.Expose
    public BlockPos add(@NonNull Vec3i vec) {
        return add(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Add the given coordinates to the coordinates of this BlockPos
     */
    @StableAPI.Expose
    public BlockPos add(int x, int y, int z) {
        if (x == 0 && y == 0 && z == 0) {
            return this;
        }
        return new BlockPos(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Subtract the given Vector from this BlockPos
     */
    @StableAPI.Expose
    public BlockPos subtract(@NonNull Vec3i vec) {
        return add(-vec.getX(), -vec.getY(), -vec.getZ());
    }

    @StableAPI.Expose
    public BlockPos up() {
        return up(1);
    }

    @StableAPI.Expose
    public BlockPos up(int blocks) {
        return offset(UP, blocks);
    }

    @StableAPI.Expose
    public BlockPos offset(@NonNull EnumFacing facing, int blocks) {
        if (blocks == 0) {
            return this;
        }
        return new BlockPos(x + facing.getFrontOffsetX() * blocks, y + facing.getFrontOffsetY() * blocks,
                            z + facing.getFrontOffsetZ() * blocks);
    }

    @StableAPI.Expose
    public BlockPos down() {
        return down(1);
    }

    @StableAPI.Expose
    public BlockPos down(int blocks) {
        return offset(DOWN, blocks);
    }

    @StableAPI.Expose
    public BlockPos north() {
        return north(1);
    }

    @StableAPI.Expose
    public BlockPos north(int blocks) {
        return offset(NORTH, blocks);
    }

    @StableAPI.Expose
    public BlockPos south() {
        return south(1);
    }

    @StableAPI.Expose
    public BlockPos south(int blocks) {
        return offset(SOUTH, blocks);
    }

    @StableAPI.Expose
    public BlockPos west() {
        return west(1);
    }

    @StableAPI.Expose
    public BlockPos west(int blocks) {
        return offset(WEST, blocks);
    }

    @StableAPI.Expose
    public BlockPos east() {
        return east(1);
    }

    @StableAPI.Expose
    public BlockPos east(int blocks) {
        return offset(EAST, blocks);
    }

    @StableAPI.Expose
    public BlockPos offset(@NonNull EnumFacing facing) {
        return offset(facing, 1);
    }

    @StableAPI.Expose
    public BlockPos rotate(@NonNull Rotation rotation) {
        switch (rotation) {
            case NONE:
            default:
                return this;
            case CLOCKWISE_90:
                return new BlockPos(-this.getZ(), this.getY(), this.getX());
            case CLOCKWISE_180:
                return new BlockPos(-this.getX(), this.getY(), -this.getZ());
            case COUNTERCLOCKWISE_90:
                return new BlockPos(this.getZ(), this.getY(), -this.getX());
        }
    }

    @Override
    public BlockPos crossProduct(@NonNull Vec3i vec) {
        return new BlockPos(y * vec.getZ() - z * vec.getY(), z * vec.getX() - x * vec.getZ(),
                            x * vec.getY() - y * vec.getX());
    }

    @StableAPI.Expose
    public BlockPos toImmutable() {
        return this;
    }

    @Setter(onMethod_ = @StableAPI.Expose)
    @Getter(onMethod_ = @StableAPI.Expose)
    @StableAPI(since = "0.10.0")
    public static class MutableBlockPos extends BlockPos {
        /**
         * Mutable X Coordinate
         */
        protected int x;
        /**
         * Mutable Y Coordinate
         */
        protected int y;
        /**
         * Mutable Z Coordinate
         */
        protected int z;

        @StableAPI.Expose
        public MutableBlockPos() {
            this(ORIGIN);
        }

        @StableAPI.Expose
        public MutableBlockPos(@NonNull BlockPos blockPos) {
            this(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }

        @StableAPI.Expose
        public MutableBlockPos(int x, int y, int z) {
            super(ORIGIN);
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public BlockPos add(double x, double y, double z) {
            return super.add(x, y, z).toImmutable();
        }

        @Override
        public BlockPos add(int x, int y, int z) {
            return super.add(x, y, z).toImmutable();
        }

        @Override
        public BlockPos offset(@NonNull EnumFacing facing, int blocks) {
            return super.offset(facing, blocks).toImmutable();
        }

        @Override
        public BlockPos rotate(@NonNull Rotation rotation) {
            return super.rotate(rotation).toImmutable();
        }

        @StableAPI.Expose
        public BlockPos toImmutable() {
            return new BlockPos(this);
        }

        public BlockPos.MutableBlockPos setPos(@NonNull Entity entity) {
            return this.setPos(entity.posX, entity.posY, entity.posZ);
        }

        @StableAPI.Expose
        public BlockPos.MutableBlockPos setPos(double x, double y, double z) {
            return setPos(MathUtil.floor(x), MathUtil.floor(y), MathUtil.floor(z));
        }

        @StableAPI.Expose
        public BlockPos.MutableBlockPos setPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        @StableAPI.Expose
        public BlockPos.MutableBlockPos setPos(@NonNull Vec3i vec) {
            return this.setPos(vec.getX(), vec.getY(), vec.getZ());
        }

        @StableAPI.Expose
        public BlockPos.MutableBlockPos move(@NonNull EnumFacing facing) {
            return this.move(facing, 1);
        }

        @StableAPI.Expose
        public BlockPos.MutableBlockPos move(@NonNull EnumFacing facing, int blocks) {
            return this.setPos(x + facing.getFrontOffsetX() * blocks, y + facing.getFrontOffsetY() * blocks,
                               z + facing.getFrontOffsetZ() * blocks);
        }
    }

    @StableAPI(since = "0.10.0")
    public static final class PooledMutableBlockPos extends BlockPos.MutableBlockPos {
        private static final List<BlockPos.PooledMutableBlockPos> POOL = new ArrayList<>();
        private boolean released;

        private PooledMutableBlockPos(int x, int y, int z) {
            super(x, y, z);
        }

        @StableAPI.Expose
        public static BlockPos.PooledMutableBlockPos retain() {
            return retain(0, 0, 0);
        }

        @StableAPI.Expose
        public static BlockPos.PooledMutableBlockPos retain(int x, int y, int z) {
            synchronized (POOL) {
                if (!POOL.isEmpty()) {
                    val pos = POOL.remove(POOL.size() - 1);
                    if (pos != null && pos.released) {
                        pos.released = false;
                        pos.setPos(x, y, z);
                        return pos;
                    }
                }
            }
            return new BlockPos.PooledMutableBlockPos(x, y, z);
        }

        @StableAPI.Expose
        public static BlockPos.PooledMutableBlockPos retain(double x, double y, double z) {
            return retain(MathUtil.floor(x), MathUtil.floor(y), MathUtil.floor(z));
        }

        @StableAPI.Expose
        public static BlockPos.PooledMutableBlockPos retain(@NonNull Vec3i vec) {
            return retain(vec.getX(), vec.getY(), vec.getZ());
        }

        @StableAPI.Expose
        public void release() {
            synchronized (POOL) {
                if (POOL.size() < 100) {
                    POOL.add(this);
                }
                this.released = true;
            }
        }

        @Override
        public BlockPos.PooledMutableBlockPos setPos(@NonNull Entity entity) {
            return (BlockPos.PooledMutableBlockPos) super.setPos(entity);
        }

        @Override
        public BlockPos.PooledMutableBlockPos setPos(double x, double y, double z) {
            return (BlockPos.PooledMutableBlockPos) super.setPos(x, y, z);
        }

        @Override
        public BlockPos.PooledMutableBlockPos setPos(int x, int y, int z) {
            if (released) {
                BlockPos.LOGGER.error("PooledMutableBlockPosition modified after it was released.", new Throwable());
                released = false;
            }
            return (BlockPos.PooledMutableBlockPos) super.setPos(x, y, z);
        }

        @Override
        public BlockPos.PooledMutableBlockPos setPos(@NonNull Vec3i vec) {
            return (BlockPos.PooledMutableBlockPos) super.setPos(vec);
        }

        @Override
        public BlockPos.PooledMutableBlockPos move(@NonNull EnumFacing facing) {
            return (BlockPos.PooledMutableBlockPos) super.move(facing);
        }

        @Override
        public BlockPos.PooledMutableBlockPos move(@NonNull EnumFacing facing, int blocks) {
            return (BlockPos.PooledMutableBlockPos) super.move(facing, blocks);
        }
    }
}