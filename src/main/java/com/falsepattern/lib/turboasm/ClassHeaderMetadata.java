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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Utilities for quickly processing class files without fully parsing them.
 */
public final class ClassHeaderMetadata implements FastClassAccessor {

    public final int minorVersion;
    public final int majorVersion;
    public final int constantPoolEntryCount;
    /** Byte offsets of where each constant pool entry starts (index of the tag byte), zero-indexed! */
    public final int @NotNull [] constantPoolEntryOffsets;
    /** Type of each parsed constant pool entry, zero-indexed! */
    public final ConstantPoolEntryTypes @NotNull [] constantPoolEntryTypes;

    public final int constantPoolEndOffset;
    public final int accessFlags;
    public final int thisClassIndex;
    public final int superClassIndex;
    public final int interfacesCount;
    public final @NotNull String binaryThisName;
    public final @Nullable String binarySuperName;

    /**
     * Attempts to parse a class header.
     * @param bytes The class bytes to parse.
     */
    public ClassHeaderMetadata(byte @NotNull [] bytes) {
        if (!isValidClass(bytes)) {
            throw new IllegalArgumentException("Invalid class detected");
        }
        this.minorVersion = u16(bytes, Offsets.minorVersionU16);
        this.majorVersion = u16(bytes, Offsets.majorVersionU16);
        this.constantPoolEntryCount = u16(bytes, Offsets.constantPoolCountU16);
        this.constantPoolEntryOffsets = new int[constantPoolEntryCount];
        this.constantPoolEntryTypes = new ConstantPoolEntryTypes[constantPoolEntryCount];
        // scan through CP entries
        final int cpOff;
        {
            int off = Offsets.constantPoolStart;
            for (int entry = 0; entry < constantPoolEntryCount - 1; entry++) {
                constantPoolEntryOffsets[entry] = off;
                ConstantPoolEntryTypes type = ConstantPoolEntryTypes.parse(bytes, off);
                constantPoolEntryTypes[entry] = type;
                if (type == ConstantPoolEntryTypes.Double || type == ConstantPoolEntryTypes.Long) {
                    // Longs and Doubles take up 2 constant pool indices
                    entry++;
                    constantPoolEntryOffsets[entry] = off;
                    constantPoolEntryTypes[entry] = type;
                }
                off += type.byteLength(bytes, off);
            }
            cpOff = off;
            this.constantPoolEndOffset = cpOff;
        }
        this.accessFlags = u16(bytes, cpOff + Offsets.pastCpAccessFlagsU16);
        this.thisClassIndex = u16(bytes, cpOff + Offsets.pastCpThisClassU16);
        this.superClassIndex = u16(bytes, cpOff + Offsets.pastCpSuperClassU16);
        this.interfacesCount = u16(bytes, cpOff + Offsets.pastCpInterfacesCountU16);
        // Parse this&super names
        if (constantPoolEntryTypes[thisClassIndex - 1] != ConstantPoolEntryTypes.Class) {
            throw new IllegalArgumentException("This class index is not a class ref");
        }
        final int thisNameIndex = u16(bytes, constantPoolEntryOffsets[thisClassIndex - 1] + 1);
        if (constantPoolEntryTypes[thisNameIndex - 1] != ConstantPoolEntryTypes.Utf8) {
            throw new IllegalArgumentException("This class index does not point to a UTF8 entry");
        }
        this.binaryThisName = modifiedUtf8(bytes, constantPoolEntryOffsets[thisNameIndex - 1] + 1);
        if (superClassIndex == 0) {
            // Should only be true for this==java/lang/Object
            this.binarySuperName = null;
        } else {
            final int superNameIndex = u16(bytes, constantPoolEntryOffsets[superClassIndex - 1] + 1);
            if (constantPoolEntryTypes[superClassIndex - 1] != ConstantPoolEntryTypes.Class) {
                throw new IllegalArgumentException("Super class index is not a class ref");
            }
            if (constantPoolEntryTypes[superNameIndex - 1] != ConstantPoolEntryTypes.Utf8) {
                throw new IllegalArgumentException("Super class index does not point to a UTF8 entry");
            }
            this.binarySuperName = modifiedUtf8(bytes, constantPoolEntryOffsets[superNameIndex - 1] + 1);
        }
    }

    /** Helpers to read big-endian values from class files. */
    public static int u8(byte @NotNull [] arr, int off) {
        return ((int) arr[off]) & 0xff;
    }
    /** Helpers to read big-endian values from class files. */
    public static int u16(byte @NotNull [] arr, int off) {
        return (u8(arr, off) << 8) | (u8(arr, off + 1));
    }

    /**
     * Reads "modified UTF8" (16-bit length + variable-length data) used by Java to encode String constants in class files.
     * @param arr The byte array to read from.
     * @param off Offset to the 16-bit length field.
     * @return The decoded String.
     */
    public static @NotNull String modifiedUtf8(byte @NotNull [] arr, int off) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(arr, off, arr.length - off);
                DataInputStream dis = new DataInputStream(bais)) {
            return dis.readUTF();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Header offsets from <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-2.html#jvms-2.1">JVMS for Java 21</a>
     * ClassFile {
     * u4             magic;
     * u2             minor_version;
     * u2             major_version;
     * u2             constant_pool_count;
     * cp_info        constant_pool[constant_pool_count-1];
     * u2             access_flags;
     * u2             this_class;
     * u2             super_class;
     * u2             interfaces_count;
     * u2             interfaces[interfaces_count];
     * u2             fields_count;
     * field_info     fields[fields_count];
     * u2             methods_count;
     * method_info    methods[methods_count];
     * u2             attributes_count;
     * attribute_info attributes[attributes_count];
     * }
     */
    public static final class Offsets {
        private Offsets() {}
        /** The magic item supplies the magic number identifying the class file format; it has the value 0xCAFEBABE */
        public static final int magicU32 = 0;
        /**  The values of the minor_version and major_version items are the minor and major version numbers of this class file. Together, a major and a minor version number determine the version of the class file format. If a class file has major version number M and minor version number m, we denote the version of its class file format as M.m */
        public static final int minorVersionU16 = magicU32 + 4;
        /**  The values of the minor_version and major_version items are the minor and major version numbers of this class file. Together, a major and a minor version number determine the version of the class file format. If a class file has major version number M and minor version number m, we denote the version of its class file format as M.m */
        public static final int majorVersionU16 = minorVersionU16 + 2;
        /**  The value of the constant_pool_count item is equal to the number of entries in the constant_pool table plus one. A constant_pool index is considered valid if it is greater than zero and less than constant_pool_count, with the exception for constants of type long and double noted in §4.4.5. */
        public static final int constantPoolCountU16 = majorVersionU16 + 2;
        /**
         * The constant_pool is a table of structures (§4.4) representing various string constants, class and interface names, field names, and other constants that are referred to within the ClassFile structure and its substructures. The format of each constant_pool table entry is indicated by its first "tag" byte.
         * <p>
         * The constant_pool table is indexed from 1 to constant_pool_count - 1.
         */
        public static final int constantPoolStart = constantPoolCountU16 + 2;
        /**
         * The value of the access_flags item is a mask of flags used to denote access permissions to and properties of this class or interface. The interpretation of each flag, when set, is specified in Table 4.1-B.
         */
        public static final int pastCpAccessFlagsU16 = 0;
        /**
         *  The value of the this_class item must be a valid index into the constant_pool table. The constant_pool entry at that index must be a CONSTANT_Class_info structure (§4.4.1) representing the class or interface defined by this class file.
         */
        public static final int pastCpThisClassU16 = pastCpAccessFlagsU16 + 2;
        /**
         * For a class, the value of the super_class item either must be zero or must be a valid index into the constant_pool table. If the value of the super_class item is nonzero, the constant_pool entry at that index must be a CONSTANT_Class_info structure representing the direct superclass of the class defined by this class file. Neither the direct superclass nor any of its superclasses may have the ACC_FINAL flag set in the access_flags item of its ClassFile structure. <p>
         * If the value of the super_class item is zero, then this class file must represent the class Object, the only class or interface without a direct superclass. <p>
         * For an interface, the value of the super_class item must always be a valid index into the constant_pool table. The constant_pool entry at that index must be a CONSTANT_Class_info structure representing the class Object.
         */
        public static final int pastCpSuperClassU16 = pastCpThisClassU16 + 2;
        /** The value of the interfaces_count item gives the number of direct superinterfaces of this class or interface type */
        public static final int pastCpInterfacesCountU16 = pastCpSuperClassU16 + 2;
    }

    public enum ConstantPoolEntryTypes {
        Utf8(1, 45, -1),
        Integer(3, 45, 4),
        Float(4, 45, 4),
        Long(5, 45, 8),
        Double(6, 45, 8),
        Class(7, 49, 2),
        String(8, 45, 2),
        FieldRef(9, 45, 4),
        MethodRef(10, 45, 4),
        InterfaceMethodRef(11, 45, 4),
        NameAndType(12, 45, 4),
        MethodHandle(15, 51, 3),
        MethodType(16, 51, 2),
        Dynamic(17, 55, 4),
        InvokeDynamic(18, 51, 4),
        Module(19, 53, 2),
        Package(20, 53, 2),
        ;
        private static final ConstantPoolEntryTypes[] lookup;

        static {
            final ConstantPoolEntryTypes[] values = values();
            final int maxTag = values[values.length - 1].tag;
            final ConstantPoolEntryTypes[] lut = new ConstantPoolEntryTypes[maxTag + 1];
            for (ConstantPoolEntryTypes entry : values) {
                lut[entry.tag] = entry;
            }
            lookup = lut;
        }

        public final int tag, minMajorVersion;
        /** Length in bytes of the entry (excluding the 1 byte tag), or -1 if length-encoded */
        public final int maybeByteLength;

        ConstantPoolEntryTypes(int tag, int minMajorVersion, int maybeByteLength) {
            this.tag = tag;
            this.minMajorVersion = minMajorVersion;
            this.maybeByteLength = maybeByteLength;
        }

        /**
         * @param classFile Class bytes to scan for variable length entries
         * @param offset The offset where the entry starts
         * @return The total length of the entry, including the tag byte
         */
        public int byteLength(final byte @NotNull [] classFile, final int offset) {
            if (this == ConstantPoolEntryTypes.Utf8) {
                return 3 + u16(classFile, offset + 1);
            }
            if (this.maybeByteLength < 0) {
                throw new UnsupportedOperationException("Missing byte length implementation for tag " + this);
            }
            return 1 + maybeByteLength;
        }

        public static @NotNull ConstantPoolEntryTypes parse(final byte @NotNull [] classFile, final int offset) {
            final int tag = u8(classFile, offset);
            final ConstantPoolEntryTypes type = tag >= lookup.length ? null : lookup[tag];
            if (type == null) {
                throw new UnsupportedOperationException("Unknown constant pool tag " + tag);
            }
            return type;
        }
    }

    /**
     * Sanity-checks the validity of the class header.
     * @param classBytes Class data
     * @return If the class passes simple sanity checks.
     */
    @Contract("null -> false")
    public static boolean isValidClass(byte @Nullable [] classBytes) {
        if (classBytes == null) {
            return false;
        }
        if (classBytes.length < Offsets.constantPoolStart + Offsets.pastCpSuperClassU16) {
            return false;
        }
        final int magic =
                (u8(classBytes, 0) << 24) | (u8(classBytes, 1) << 16) | (u8(classBytes, 2) << 8) | (u8(classBytes, 3));
        return magic == 0xCAFEBABE;
    }

    /**
     * @param classBytes Class data
     * @return The major version number of the class file. See {@link org.objectweb.asm.Opcodes#V1_8}
     */
    public static int majorVersion(byte @NotNull [] classBytes) {
        return u16(classBytes, Offsets.majorVersionU16);
    }

    /**
     * Searches for a sub"string" (byte array) in a longer byte array. Not efficient for long search strings.
     * @param classBytes The long byte string to search in.
     * @param substring The short substring to search for.
     * @return If the substring was found somewhere in the long string.
     */
    public static boolean hasSubstring(final byte @Nullable [] classBytes, final byte @NotNull [] substring) {
        if (classBytes == null) {
            return false;
        }
        final int classLen = classBytes.length;
        final int subLen = substring.length;
        if (classLen < subLen) {
            return false;
        }
        outer:
        for (int startPos = 0; startPos + subLen - 1 < classLen; startPos++) {
            for (int i = 0; i < subLen; i++) {
                if (classBytes[startPos + i] != substring[i]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isPublic() {
        return (accessFlags & Opcodes.ACC_PUBLIC) != 0;
    }

    @Override
    public boolean isFinal() {
        return (accessFlags & Opcodes.ACC_FINAL) != 0;
    }

    @Override
    public boolean isInterface() {
        return (accessFlags & Opcodes.ACC_INTERFACE) != 0;
    }

    @Override
    public boolean isAbstract() {
        return (accessFlags & Opcodes.ACC_ABSTRACT) != 0;
    }

    @Override
    public boolean isSynthetic() {
        return (accessFlags & Opcodes.ACC_SYNTHETIC) != 0;
    }

    @Override
    public boolean isAnnotation() {
        return (accessFlags & Opcodes.ACC_ANNOTATION) != 0;
    }

    @Override
    public boolean isEnum() {
        return (accessFlags & Opcodes.ACC_ENUM) != 0;
    }

    @Override
    public @NotNull String binaryThisName() {
        return binaryThisName;
    }

    @Override
    public @Nullable String binarySuperName() {
        return binarySuperName;
    }
}