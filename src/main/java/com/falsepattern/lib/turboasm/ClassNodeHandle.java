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

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/** A simple handle to a mutable ClassNode and flags for ClassWriter. */
public final class ClassNodeHandle {
    private final byte @Nullable [] originalBytes;
    private final @Nullable ClassHeaderMetadata originalMetadata;
    private final int readerOptions;
    private boolean initialized = false;
    private @Nullable ClassNode node = null;
    private @Nullable FastClassAccessor accessor = null;
    private int writerFlags = 0;

    /** Parse the class data with no reader options (for fastest speed). */
    public ClassNodeHandle(byte @Nullable [] classData) {
        this(classData, 0);
    }

    /** Parse the class data with custom reader options. */
    public ClassNodeHandle(
            byte @Nullable [] classData, @MagicConstant(flagsFromClass = ClassReader.class) int readerOptions) {
        @Nullable ClassHeaderMetadata originalMetadata;
        this.originalBytes = classData;
        if (classData == null) {
            originalMetadata = null;
        } else {
            try {
                originalMetadata = new ClassHeaderMetadata(classData);
            } catch (Exception e) {
                originalMetadata = null;
            }
        }
        this.originalMetadata = originalMetadata;
        this.accessor = originalMetadata;
        this.readerOptions = 0;
    }

    /** Gets the original pre-transformer-phase bytes of the class. */
    public byte @Nullable [] getOriginalBytes() {
        return originalBytes;
    }

    /** Gets the original pre-transformer-phase header metadata of the class, or null if invalid/not present. */
    public @Nullable ClassHeaderMetadata getOriginalMetadata() {
        return originalMetadata;
    }

    /** Gets the fast class metadata accessor of the class, that can access the current state of various class attributes without (re)parsing. */
    public @Nullable FastClassAccessor getFastAccessor() {
        return accessor;
    }

    /** @return If the class was not yet turned into a ClassNode object, and the original bytes still represent the class. */
    public boolean isOriginal() {
        return !initialized;
    }

    /** If the class currently has any bytes or a node associated with it. */
    public boolean isPresent() {
        if (initialized) {
            return node != null;
        } else {
            return originalBytes != null;
        }
    }

    /** Gets the parsed node of the currently processed class. This can cause full class parsing! */
    public @Nullable ClassNode getNode() {
        ensureInitialized();
        return node;
    }

    /** Overwrites the parsed node of the currently processed class. */
    public void setNode(@Nullable ClassNode node) {
        initialized = true;
        this.node = node;
        if (node == null) {
            this.accessor = null;
        } else {
            this.accessor = FastClassAccessor.ofAsmNode(node);
        }
    }

    /** Computes the byte[] array of the transformed class. Returns the original bytes if {@link ClassNodeHandle#getNode()} was never called. */
    public byte @Nullable [] computeBytes() {
        if (!initialized) {
            return originalBytes;
        }
        if (node == null) {
            return null;
        }
        final ClassWriter writer = new ClassWriter(writerFlags);
        node.accept(writer);
        return writer.toByteArray();
    }

    /** Gets the ClassWriter flags for the current class. */
    public int getWriterFlags() {
        return writerFlags;
    }

    /** Set the ClassWriter flags for the current class. */
    public void setWriterFlags(@MagicConstant(flagsFromClass = ClassWriter.class) int flags) {
        this.writerFlags = flags;
    }

    /** Combine the currently set writer flags with the given flags using bitwise OR. */
    public void orWriterFlags(@MagicConstant(flagsFromClass = ClassWriter.class) int flags) {
        this.writerFlags |= flags;
    }

    /** Set ClassWriter.COMPUTE_MAXS on the writer flags. */
    public void computeMaxs() {
        this.writerFlags |= ClassWriter.COMPUTE_MAXS;
    }

    /** Set ClassWriter.COMPUTE_FRAMES on the writer flags. */
    public void computeFrames() {
        this.writerFlags |= ClassWriter.COMPUTE_FRAMES;
    }

    private void ensureInitialized() {
        if (!initialized) {
            if (originalBytes == null) {
                node = null;
                accessor = null;
            } else {
                node = new ClassNode();
                new ClassReader(originalBytes).accept(node, readerOptions);
                accessor = FastClassAccessor.ofAsmNode(node);
            }
            initialized = true;
        }
    }
}