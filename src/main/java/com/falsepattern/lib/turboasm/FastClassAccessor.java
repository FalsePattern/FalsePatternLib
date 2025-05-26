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
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Modifier;

/** An accessor to metadata about a class that is quickly accessible without fully parsing one. */
public interface FastClassAccessor {
    /** Accessible from outside its package */
    boolean isPublic();
    /** No subclasses allowed */
    boolean isFinal();
    /** Is an interface instead of a class */
    boolean isInterface();
    /** Is an abstract class that should not be instantiated */
    boolean isAbstract();
    /** Is not present in source code (often used by obfuscated code too) */
    boolean isSynthetic();
    /** Is an annotation interface */
    boolean isAnnotation();
    /** Is an enum class */
    boolean isEnum();
    /** Binary (slash-separated packages) name of the class */
    @NotNull
    String binaryThisName();
    /** Binary (slash-separated packages) name of the super-class, null for the Object class */
    @Nullable
    String binarySuperName();

    static OfLoaded ofLoaded(Class<?> loadedClass) {
        return new OfLoaded(loadedClass);
    }

    static OfAsmNode ofAsmNode(ClassNode handle) {
        return new OfAsmNode(handle);
    }

    final class OfAsmNode implements FastClassAccessor {
        public final ClassNode handle;

        public OfAsmNode(ClassNode handle) {
            this.handle = handle;
        }

        @Override
        public boolean isPublic() {
            return (handle.access & Opcodes.ACC_PUBLIC) != 0;
        }

        @Override
        public boolean isFinal() {
            return (handle.access & Opcodes.ACC_FINAL) != 0;
        }

        @Override
        public boolean isInterface() {
            return (handle.access & Opcodes.ACC_INTERFACE) != 0;
        }

        @Override
        public boolean isAbstract() {
            return (handle.access & Opcodes.ACC_ABSTRACT) != 0;
        }

        @Override
        public boolean isSynthetic() {
            return (handle.access & Opcodes.ACC_SYNTHETIC) != 0;
        }

        @Override
        public boolean isAnnotation() {
            return (handle.access & Opcodes.ACC_ANNOTATION) != 0;
        }

        @Override
        public boolean isEnum() {
            return (handle.access & Opcodes.ACC_ENUM) != 0;
        }

        @Override
        public @NotNull String binaryThisName() {
            return handle.name;
        }

        @Override
        public @Nullable String binarySuperName() {
            return handle.superName;
        }
    }

    final class OfLoaded implements FastClassAccessor {
        public final Class<?> handle;

        private OfLoaded(Class<?> handle) {
            this.handle = handle;
        }

        @Override
        public boolean isPublic() {
            return Modifier.isPublic(handle.getModifiers());
        }

        @Override
        public boolean isFinal() {
            return Modifier.isFinal(handle.getModifiers());
        }

        @Override
        public boolean isInterface() {
            return Modifier.isInterface(handle.getModifiers());
        }

        @Override
        public boolean isAbstract() {
            return Modifier.isAbstract(handle.getModifiers());
        }

        @Override
        public boolean isSynthetic() {
            return handle.isSynthetic();
        }

        @Override
        public boolean isAnnotation() {
            return handle.isAnnotation();
        }

        @Override
        public boolean isEnum() {
            return handle.isEnum();
        }

        @Override
        public @NotNull String binaryThisName() {
            return handle.getName().replace('.', '/');
        }

        @Override
        public @Nullable String binarySuperName() {
            final Class<?> superclass = handle.getSuperclass();
            return superclass == null ? null : superclass.getName().replace('.', '/');
        }
    }
}