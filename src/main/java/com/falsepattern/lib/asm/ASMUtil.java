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
package com.falsepattern.lib.asm;

import com.falsepattern.lib.asm.exceptions.AsmClassNotFoundException;
import com.falsepattern.lib.asm.exceptions.AsmFieldNotFoundException;
import com.falsepattern.lib.asm.exceptions.AsmMethodNotFoundException;
import com.falsepattern.lib.internal.CoreLoadingPlugin;
import com.falsepattern.lib.mapping.MappingManager;
import com.falsepattern.lib.mapping.types.MappingType;
import com.falsepattern.lib.mapping.types.NameType;
import com.falsepattern.lib.mapping.types.UniversalClass;
import com.falsepattern.lib.mapping.types.UniversalField;
import com.falsepattern.lib.mapping.types.UniversalMethod;
import lombok.SneakyThrows;
import lombok.val;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.Objects;

public class ASMUtil {
    public static FieldNode findFieldStandard(ClassNode cn, String name, boolean optional) {
        for (final FieldNode ret : cn.fields) {
            if (name.equals(ret.name)) {
                return ret;
            }
        }
        if (optional) {
            return null;
        }
        throw new AsmFieldNotFoundException(name);
    }

    @SneakyThrows
    public static FieldNode findFieldFromMCP(ClassNode cn, String fieldName, boolean optional) {
        val classMapping = discoverClassMappingType(cn);
        if (classMapping == null) {
            throw new AsmClassNotFoundException("The class " + cn + " is not from Minecraft, or the mapping manager" +
                                                "doesn't have it, cannot use findFieldFromMCP! Use findFieldStandard instead!");
        }
        return findFieldStandard(cn, MappingManager.classForName(NameType.Internal, classMapping, cn.name)
                                                   .getField(MappingType.MCP, fieldName)
                                                   .getName(classMapping), optional);
    }

    public static FieldNode findFieldFromUniversal(ClassNode cn, UniversalField field, boolean optional) {
        String[] possibilities = CoreLoadingPlugin.isObfuscated() ? new String[]{field.getName(MappingType.SRG),
                                                                                 field.getName(MappingType.Notch)}
                                                                  : new String[]{field.getName(MappingType.MCP)};
        for (final FieldNode ret : cn.fields) {
            if (anyEquals(ret.name, possibilities)) {
                return ret;
            }
        }
        if (optional) {
            return null;
        }
        throw new AsmFieldNotFoundException(
                possibilities.length == 1 ? possibilities[0] : Arrays.toString(possibilities));
    }

    public static MethodNode findMethodStandard(ClassNode cn, String name, String descriptor, boolean optional) {
        for (final MethodNode ret : cn.methods) {
            if (name.equals(ret.name) && descriptor.equals(ret.desc)) {
                return ret;
            }
        }
        if (optional) {
            return null;
        }
        throw new AsmMethodNotFoundException(name);
    }

    @SneakyThrows
    public static MethodNode findMethodFromMCP(ClassNode cn, String mcpName, String mcpDescriptor, boolean optional) {
        val classMapping = discoverClassMappingType(cn);
        if (classMapping == null) {
            throw new AsmClassNotFoundException("The class " + cn + " is not from Minecraft, or the mapping manager" +
                                                "doesn't have it, cannot use findMethodFromMCP! Use findFieldStandard instead!");
        }
        val method = MappingManager.classForName(NameType.Internal, classMapping, cn.name)
                                   .getMethod(MappingType.MCP, mcpName, mcpDescriptor);
        return findMethodStandard(cn, method.getName(classMapping), method.getDescriptor(classMapping), optional);
    }

    public static MethodNode findMethodFromUniversal(ClassNode cn, UniversalMethod method, boolean optional) {
        String[] possibleNames = CoreLoadingPlugin.isObfuscated() ? new String[]{method.getName(MappingType.SRG),
                                                                                 method.getName(MappingType.Notch)}
                                                                  : new String[]{method.getName(MappingType.MCP)};
        String[] possibleDescriptors =
                CoreLoadingPlugin.isObfuscated() ? new String[]{method.getDescriptor(MappingType.SRG),
                                                                method.getDescriptor(MappingType.Notch)}
                                                 : new String[]{method.getDescriptor(MappingType.MCP)};
        for (final MethodNode ret : cn.methods) {
            if (anyEquals(ret.name, possibleNames) && anyEquals(ret.desc, possibleDescriptors)) {
                return ret;
            }
        }
        if (optional) {
            return null;
        }
        throw new AsmFieldNotFoundException(
                possibleDescriptors.length == 1 ? possibleDescriptors[0] : Arrays.toString(possibleDescriptors));
    }

    public static MappingType discoverClassMappingType(ClassNode cn) {
        if (CoreLoadingPlugin.isObfuscated()) {
            if (MappingManager.containsClass(NameType.Internal, MappingType.MCP, cn.name)) {
                return MappingType.MCP;
            }
        } else if (MappingManager.containsClass(NameType.Internal, MappingType.SRG, cn.name)) {
            return MappingType.SRG;
        } else if (MappingManager.containsClass(NameType.Internal, MappingType.Notch, cn.name)) {
            return MappingType.Notch;
        }
        return null;
    }

    public static UniversalClass toUniversalClass(ClassNode cn) {
        if (CoreLoadingPlugin.isObfuscated()) {
            try {
                return MappingManager.classForName(NameType.Internal, MappingType.MCP, cn.name);
            } catch (ClassNotFoundException e) {
                throw new AsmClassNotFoundException(cn.name);
            }
        } else {
            try {
                return MappingManager.classForName(NameType.Internal, MappingType.SRG, cn.name);
            } catch (ClassNotFoundException e) {
                try {
                    return MappingManager.classForName(NameType.Internal, MappingType.Notch, cn.name);
                } catch (ClassNotFoundException ex) {
                    throw new AsmClassNotFoundException(cn.name);
                }
            }
        }
    }

    public static ClassNode parseClass(byte[] bytes, int readerFlags) {
        val cn = new ClassNode(Opcodes.ASM5);
        val reader = new ClassReader(bytes);
        reader.accept(cn, readerFlags);
        return cn;
    }

    public static byte[] serializeClass(ClassNode cn, int writerFlags) {
        val writer = new ClassWriter(writerFlags);
        return writer.toByteArray();
    }

    public static boolean anyEquals(String str, String... options) {
        for (val option : options) {
            if (Objects.equals(str, option)) {
                return true;
            }
        }
        return false;
    }
}
