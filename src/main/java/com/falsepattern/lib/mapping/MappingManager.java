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
package com.falsepattern.lib.mapping;

import com.falsepattern.lib.internal.CoreLoadingPlugin;
import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.mapping.storage.Lookup;
import com.falsepattern.lib.mapping.types.MappingType;
import com.falsepattern.lib.mapping.types.NameType;
import com.falsepattern.lib.mapping.types.UniversalClass;
import com.falsepattern.lib.mapping.types.UniversalField;
import com.falsepattern.lib.mapping.types.UniversalMethod;
import com.falsepattern.lib.util.ResourceUtil;
import lombok.SneakyThrows;
import lombok.val;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.HashMap;

public class MappingManager {
    private static boolean initialized = false;

    private static final Lookup<UniversalClass> internalLookup = new Lookup<>();
    private static final Lookup<UniversalClass> regularLookup = new Lookup<>();


    @SneakyThrows
    private static synchronized void initialize() {
        if (initialized) return;
        initialized = true;
        long start = System.nanoTime();
        val stringPool = new HashMap<String, String>();
        {
            val classMappings = ResourceUtil.getResourceStringFromJar("/classes.csv", FalsePatternLib.class).split("\n");
            for (int i = 1; i < classMappings.length; i++) {
                val line = classMappings[i].split(",");
                val clazz = new UniversalClass(line, stringPool);
                internalLookup.unwrap(clazz.internalName, clazz);
                regularLookup.unwrap(clazz.regularName, clazz);
            }
        }
        {
            val fieldMappings = ResourceUtil.getResourceStringFromJar("/fields.csv", FalsePatternLib.class).split("\n");
            for (int i = 1; i < fieldMappings.length; i++) {
                val line = fieldMappings[i].split(",");
                val clazz = internalLookup.get(MappingType.Notch, line[0].substring(0, line[0].lastIndexOf('/')));
                new UniversalField(clazz, line, stringPool);
            }
        }
        {
            val methodMappings = ResourceUtil.getResourceStringFromJar("/methods.csv", FalsePatternLib.class).split("\n");
            for (int i = 1; i < methodMappings.length; i++) {
                val line = methodMappings[i].split(",");
                val clazz = internalLookup.get(MappingType.Notch, line[0].substring(0, line[0].lastIndexOf('/')));
                new UniversalMethod(clazz, line, stringPool);
            }
        }
        long end = System.nanoTime();
        System.out.println("Parsed in " + (end - start) / 1000000 + "ms");
    }

    public static UniversalClass classForName(NameType nameType, MappingType mappingType, String className) throws ClassNotFoundException {
        initialize();
        try {
            switch (nameType) {
                case Internal:
                    return internalLookup.get(mappingType, className);
                case Regular:
                    return regularLookup.get(mappingType, className);
                default:
                    throw new IllegalArgumentException("Invalid enum value " + nameType);
            }
        } catch (Lookup.LookupException e) {
            throw new ClassNotFoundException("Could not find class \"" + className + "\" with " + nameType.name().toLowerCase() + " name in the " + mappingType.name() + " mapping.");
        }
    }

    public static boolean containsClass(NameType nameType, MappingType mappingType, String className) {
        switch (nameType) {
            case Internal:
                return internalLookup.containsKey(mappingType, className);
            case Regular:
                return regularLookup.containsKey(mappingType, className);
            default:
                throw new IllegalArgumentException("Invalid enum value " + nameType);
        }
    }

    public static UniversalField getField(FieldInsnNode instruction) throws ClassNotFoundException,
            NoSuchFieldException {
        if (!CoreLoadingPlugin.isObfuscated()) {
            try {
                return classForName(NameType.Internal, MappingType.MCP, instruction.owner).getField(MappingType.MCP, instruction.name);
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("Could not find the class " + instruction.owner + " in the MCP mappings. Are you sure it's a Minecraft class? (we're in dev, cannot use SRG or Notch here).");
            }
        } else {
            try {
                return classForName(NameType.Internal, MappingType.SRG, instruction.owner).getField(MappingType.SRG, instruction.name);
            } catch (ClassNotFoundException e) {
                try {
                    return classForName(NameType.Internal, MappingType.Notch, instruction.owner).getField(MappingType.Notch, instruction.name);
                } catch (ClassNotFoundException ex) {
                    throw new ClassNotFoundException("Could not find the class " + instruction.owner + " neither in the SRG nor in the Notch mappings. Are you sure it's a Minecraft class? (we're in obf, cannot use MCP here)");
                }
            }
        }
    }

    public static UniversalMethod getMethod(MethodInsnNode instruction)
            throws ClassNotFoundException, NoSuchMethodException {
        if (!CoreLoadingPlugin.isObfuscated()) {
            try {
                return classForName(NameType.Internal, MappingType.MCP, instruction.owner).getMethod(MappingType.MCP, instruction.name, instruction.desc);
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("Could not find the class " + instruction.owner + " in the MCP mappings. Are you sure it's a Minecraft class? (we're in dev, cannot use SRG or Notch here).");
            }
        } else {
            try {
                return classForName(NameType.Internal, MappingType.SRG, instruction.owner).getMethod(MappingType.SRG, instruction.name, instruction.desc);
            } catch (ClassNotFoundException e) {
                try {
                    return classForName(NameType.Internal, MappingType.Notch, instruction.owner).getMethod(MappingType.Notch, instruction.name, instruction.desc);
                } catch (ClassNotFoundException ex) {
                    throw new ClassNotFoundException("Could not find the class " + instruction.owner + " neither in the SRG nor in the Notch mappings. Are you sure it's a Minecraft class? (we're in obf, cannot use MCP here)");
                }
            }
        }
    }
}
