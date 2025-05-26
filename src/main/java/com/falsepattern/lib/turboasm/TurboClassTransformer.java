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

package com.falsepattern.lib.turboasm;

import org.jetbrains.annotations.NotNull;

/**
 * A simple transformer that takes in class bytes and outputs different class bytes.
 * It should be thread-safe, and not change class names. It should also have a public no-arguments constructor.
 */
public interface TurboClassTransformer {
    /**
     * @return The user-friendly owner name of this transformer. Usually a mod id.
     */
    String owner();

    /**
     * @return The user-friendly name of this transformer. Used in logs.
     */
    String name();

    /**
     * A fast scanning function that is used to determine if class transformations should be skipped altogether (if all transformers return false).
     * @param className The name of the transformed class (in the dot-separated format).
     * @param classNode The handle to the class data and parsed metadata, try to avoid triggering the lazy ASM parse if possible for performance.
     * @return true if the class will be transformed by this class transformer.
     */
    boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode);

    /**
     * (Optionally) transform a given class. No ClassReader flags are used for maximum efficiency, so stack frames are not expanded.
     * @param className The name of the transformed class (in the dot-separated format).
     * @param classNode The handle to the lazily ASM-parsed class to modify, and metadata used for class writing.
     * @return True if the class has been modified in any way by this transformer. If all transformers return false,
     * then the ClassNode instance will not be re-serialized.
     */
    boolean transformClass(@NotNull String className, @NotNull ClassNodeHandle classNode);
}