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

import com.falsepattern.lib.DeprecationDetails;
import com.falsepattern.lib.util.MathUtil;

import java.util.Random;

/**
 * Alert: This class will be removed in 0.11, migrate to {@link MathUtil}!
 */
@Deprecated
@DeprecationDetails(deprecatedSince = "0.10.0")
public class MathHelper {
    /**
     * Though it looks like an array, this is really more like a mapping.  Key (index of this array) is the upper 5 bits
     * of the result of multiplying a 32-bit unsigned integer by the B(2, 5) De Bruijn sequence 0x077CB531.  Value
     * (value stored in the array) is the unique index (from the right) of the leftmost one-bit in a 32-bit unsigned
     * integer that can cause the upper 5 bits to get that value.  Used for highly optimized "find the log-base-2 of
     * this number" calculations.
     */
    private static final int[] multiplyDeBruijnBitPosition;
    private static final float[] SIN_TABLE = new float[65536];

    static {
        for (int i = 0; i < 65536; ++i) {
            SIN_TABLE[i] = (float) Math.sin((double) i * Math.PI * 2.0D / 65536.0D);
        }

        multiplyDeBruijnBitPosition =
                new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26,
                          12, 18, 6, 11, 5, 10, 9};
    }

    /**
     * sin looked up in a table
     */
    public static float sin(float a) {
        return SIN_TABLE[(int) (a * 10430.378F) & 65535];
    }

    /**
     * cos looked up in the sin table with the appropriate offset
     */
    public static float cos(float a) {
        return SIN_TABLE[(int) (a * 10430.378F + 16384.0F) & 65535];
    }

    public static float sqrt_float(float a) {
        return (float) Math.sqrt(a);
    }

    public static float sqrt_double(double a) {
        return (float) Math.sqrt(a);
    }

    /**
     * Returns the greatest integer less than or equal to the float argument
     */
    public static int floor_float(float a) {
        int i = (int) a;
        return a < (float) i ? i - 1 : i;
    }

    /**
     * returns par0 cast as an int, and no greater than Integer.MAX_VALUE-1024
     */
    public static int truncateDoubleToInt(double a) {
        return (int) (a + 1024.0D) - 1024;
    }

    /**
     * Returns the greatest integer less than or equal to the double argument
     */
    public static int floor_double(double a) {
        int i = (int) a;
        return a < (double) i ? i - 1 : i;
    }

    /**
     * Long version of floor_double
     */
    public static long floor_double_long(double a) {
        long i = (long) a;
        return a < (double) i ? i - 1L : i;
    }

    public static int absFloor(double a) {
        return (int) (a >= 0.0D ? a : -a + 1.0D);
    }

    public static float abs(float a) {
        return a >= 0.0F ? a : -a;
    }

    /**
     * Returns the unsigned value of an int.
     */
    public static int abs_int(int a) {
        return a >= 0 ? a : -a;
    }

    public static int ceiling_float_int(float a) {
        int i = (int) a;
        return a > (float) i ? i + 1 : i;
    }

    public static int ceiling_double_int(double a) {
        int i = (int) a;
        return a > (double) i ? i + 1 : i;
    }

    /**
     * Returns the value of the first parameter, clamped to be within the lower and upper limits given by the second and
     * third parameters.
     */
    public static int clamp_int(int a, int min, int max) {
        return a < min ? min : (Math.min(a, max));
    }

    /**
     * Returns the value of the first parameter, clamped to be within the lower and upper limits given by the second and
     * third parameters
     */
    public static float clamp_float(float a, float min, float max) {
        return a < min ? min : Math.min(a, max);
    }

    public static double clamp_double(double a, double min, double max) {
        return a < min ? min : Math.min(a, max);
    }

    public static double denormalizeClamp(double min, double max, double normalized) {
        return normalized < 0.0D ? min : (normalized > 1.0D ? max : min + (max - min) * normalized);
    }

    /**
     * Maximum of the absolute value of two numbers.
     */
    public static double abs_max(double a, double b) {
        if (a < 0.0D) {
            a = -a;
        }

        if (b < 0.0D) {
            b = -b;
        }

        return Math.max(a, b);
    }

    /**
     * Buckets an integer with specified bucket sizes.  Args: i, bucketSize
     */
    public static int bucketInt(int i, int bucketSize) {
        return i < 0 ? -((-i - 1) / bucketSize) - 1 : i / bucketSize;
    }

    /**
     * Tests if a string is null or of length zero
     */
    public static boolean stringNullOrLengthZero(String str) {
        return str == null || str.length() == 0;
    }

    public static int getRandomIntegerInRange(Random rng, int min, int max) {
        return min >= max ? min : rng.nextInt(max - min + 1) + min;
    }

    public static float randomFloatClamp(Random rng, float min, float max) {
        return min >= max ? min : rng.nextFloat() * (max - min) + min;
    }

    public static double getRandomDoubleInRange(Random rng, double min, double max) {
        return min >= max ? min : rng.nextDouble() * (max - min) + min;
    }

    public static double average(long[] values) {
        long i = 0L;

        for (long l : values) {
            i += l;
        }

        return (double) i / (double) values.length;
    }

    /**
     * the angle is reduced to an angle between -180 and +180 by mod, and a 360 check
     */
    public static float wrapAngleTo180_float(float angle) {
        angle %= 360.0F;

        if (angle >= 180.0F) {
            angle -= 360.0F;
        }

        if (angle < -180.0F) {
            angle += 360.0F;
        }

        return angle;
    }

    /**
     * the angle is reduced to an angle between -180 and +180 by mod, and a 360 check
     */
    public static double wrapAngleTo180_double(double angle) {
        angle %= 360.0D;

        if (angle >= 180.0D) {
            angle -= 360.0D;
        }

        if (angle < -180.0D) {
            angle += 360.0D;
        }

        return angle;
    }

    /**
     * parses the string as integer or returns the second parameter if it fails
     */
    public static int parseIntWithDefault(String str, int def) {
        try {
            return Integer.parseInt(str);
        } catch (Throwable throwable) {
            return def;
        }
    }

    /**
     * parses the string as integer or returns the second parameter if it fails. this value is capped to par2
     */
    public static int parseIntWithDefaultAndMax(String str, int def, int min) {
        int k;

        try {
            k = Integer.parseInt(str);
        } catch (Throwable throwable) {
            k = def;
        }

        if (k < min) {
            k = min;
        }

        return k;
    }

    /**
     * parses the string as double or returns the second parameter if it fails.
     */
    public static double parseDoubleWithDefault(String str, double def) {
        double d1 = def;

        try {
            d1 = Double.parseDouble(str);
        } catch (Throwable ignored) {
        }

        return d1;
    }

    public static double parseDoubleWithDefaultAndMin(String str, double def, double min) {
        double d2;

        try {
            d2 = Double.parseDouble(str);
        } catch (Throwable throwable) {
            d2 = def;
        }

        if (d2 < min) {
            d2 = min;
        }

        return d2;
    }

    /**
     * Returns the input value rounded up to the next highest power of two.
     */
    public static int roundUpToPowerOfTwo(int a) {
        int j = a - 1;
        j |= j >> 1;
        j |= j >> 2;
        j |= j >> 4;
        j |= j >> 8;
        j |= j >> 16;
        return j + 1;
    }

    /**
     * Is the given value a power of two?  (1, 2, 4, 8, 16, ...)
     */
    private static boolean isPowerOfTwo(int a) {
        return a != 0 && (a & a - 1) == 0;
    }

    /**
     * Uses a B(2, 5) De Bruijn sequence and a lookup table to efficiently calculate the log-base-two of the given
     * value.  Optimized for cases where the input value is a power-of-two.  If the input value is not a power-of-two,
     * then subtract 1 from the return value.
     */
    private static int calculateLogBaseTwoDeBruijn(int a) {
        a = isPowerOfTwo(a) ? a : roundUpToPowerOfTwo(a);
        return multiplyDeBruijnBitPosition[(int) ((long) a * 125613361L >> 27) & 31];
    }

    /**
     * Efficiently calculates the floor of the base-2 log of an integer value.  This is effectively the index of the
     * highest bit that is set.  For example, if the number in binary is 0...100101, this will return 5.
     */
    public static int calculateLogBaseTwo(int a) {
        /*
         * Uses a B(2, 5) De Bruijn sequence and a lookup table to efficiently calculate the log-base-two of the given
         * value.  Optimized for cases where the input value is a power-of-two.  If the input value is not a power-of-
         * two, then subtract 1 from the return value.
         */
        return calculateLogBaseTwoDeBruijn(a) - (isPowerOfTwo(a) ? 0 : 1);
    }

    public static int roundUp(int a, int b) {
        if (b == 0) {
            return 0;
        } else {
            if (a < 0) {
                b *= -1;
            }

            int k = a % b;
            return k == 0 ? a : a + b - k;
        }
    }
}
