package com.falsepattern.lib.compat;

import com.falsepattern.lib.StableAPI;
import lombok.*;
import net.minecraft.entity.Entity;

import javax.annotation.concurrent.Immutable;

/**
 * A functional equivalent to ChunkPos present in Minecraft 1.12.
 */
@Immutable
@EqualsAndHashCode
@AllArgsConstructor
@StableAPI(since = "0.6.0")
public class ChunkPos {
    /**
     * The x position of the chunk.
     */
    public final int x;
    /**
     * The z position of the chunk.
     */
    public final int z;

    /**
     * Instantiates a new ChunkPos.
     *
     * @param blockPos the block pos
     */
    public ChunkPos(@NonNull BlockPos blockPos) {
        x = blockPos.getX() >> 4;
        z = blockPos.getZ() >> 4;
    }

    /**
     * Converts the chunk coordinate pair to a long
     *
     * @param x the chunk x
     * @param z the chunk x
     * @return the unique chunk long
     */
    public static long asLong(int x, int z) {
        return (long) x & 0xFFFFFFFFL | ((long) z & 0xFFFFFFFFL) << 32;
    }

    /**
     * Gets distance sq.
     *
     * @param entity the entity
     * @return the distance sq
     */
    public double getDistanceSq(@NonNull Entity entity) {
        val dX = ((x << 4) + 8) - entity.posX;
        val dY = ((z << 4) + 8) - entity.posZ;
        return dX * dX + dY * dY;
    }

    /**
     * Get the first x pos inside this chunk.
     *
     * @return the x start
     */
    public int getXStart() {
        return x << 4;
    }

    /**
     * Get the first z pos inside this chunk.
     *
     * @return the z start
     */
    public int getZStart() {
        return z << 4;
    }

    /**
     * Get the last x pos inside this chunk.
     *
     * @return the x end
     */
    public int getXEnd() {
        return (x << 4) + 15;
    }

    /**
     * Get the last z pos inside this chunk.
     *
     * @return the z end
     */
    public int getZEnd() {
        return (z << 4) + 15;
    }

    /**
     * Get BlockPos with respect to this chunk.
     *
     * @param x the x pos
     * @param y the y pos
     * @param z the z pos
     * @return the relative block position
     */
    public BlockPos getBlock(int x, int y, int z) {
        return new BlockPos((this.x << 4) + x, y, (this.z << 4) + z);
    }

    @Override
    public String toString() {
        return String.format("[%d, %d]", x, z);
    }
}