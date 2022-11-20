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
package com.falsepattern.lib.mapping;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.dependencies.DependencyLoader;
import com.falsepattern.lib.dependencies.Library;
import com.falsepattern.lib.dependencies.SemanticVersion;
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.asm.CoreLoadingPlugin;
import com.falsepattern.lib.mapping.moveme.ThrowingBIFunction;
import com.falsepattern.lib.mapping.storage.Lookup;
import com.falsepattern.lib.mapping.types.*;
import com.falsepattern.lib.util.ResourceUtil;
import lombok.*;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.tukaani.xz.LZMA2Options;

import java.io.DataInputStream;
import java.util.HashMap;
import java.util.Map;

@StableAPI(since = "0.10.0")
public class MappingManager {
    private static final Lookup<UniversalClass> internalLookup = new Lookup<>();
    private static final Lookup<UniversalClass> regularLookup = new Lookup<>();
    private static final Map<String, String> stringPool = new HashMap<>();
    private static boolean initialized = false;

    @SneakyThrows
    private static synchronized void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        DependencyLoader.addMavenRepo("https://repo1.maven.org/maven2/");
        DependencyLoader.loadLibraries(Library.builder()
                                              .loadingModId(Tags.MODID)
                                              .groupId("org.tukaani")
                                              .artifactId("xz")
                                              .minVersion(SemanticVersion.builder()
                                                                         .majorVersion(1)
                                                                         .minorVersion(9)
                                                                         .patchVersion(-1)
                                                                         .build())
                                              .preferredVersion(SemanticVersion.builder()
                                                                               .majorVersion(1)
                                                                               .minorVersion(9)
                                                                               .patchVersion(-1)
                                                                               .build())
                                              .build());
        val input = new DataInputStream(new LZMA2Options(6).getInputStream(
                ResourceUtil.getResourceFromJar("/mappings.lzma2", CoreLoadingPlugin.class)));
        {
            var classBytes = new byte[input.readInt()];
            input.readFully(classBytes);
            val classMappings = new String(classBytes).split("\n");
            for (int i = 1; i < classMappings.length; i++) {
                val line = classMappings[i].split(",");
                val clazz = new UniversalClass(line, stringPool);
                internalLookup.unwrap(clazz.internalName, clazz);
                regularLookup.unwrap(clazz.regularName, clazz);
            }
        }
        {
            var fieldBytes = new byte[input.readInt()];
            input.readFully(fieldBytes);
            var fieldMappings = new String(fieldBytes).split("\n");
            for (int i = 1; i < fieldMappings.length; i++) {
                val line = fieldMappings[i].split(",");
                val clazz = internalLookup.get(MappingType.Notch, line[0].substring(0, line[0].lastIndexOf('/')));
                UniversalField.createAndAddToParent(clazz, line, stringPool);
            }
        }
        {
            var methodBytes = new byte[input.readInt()];
            input.readFully(methodBytes);
            val methodMappings = new String(methodBytes).split("\n");
            for (int i = 1; i < methodMappings.length; i++) {
                val line = methodMappings[i].split(",");
                val clazz = internalLookup.get(MappingType.Notch, line[0].substring(0, line[0].lastIndexOf('/')));
                UniversalMethod.createAndAddToParent(clazz, line, stringPool);
            }
        }
    }

    @StableAPI.Expose
    public static UniversalField getField(String className, String fieldName)
            throws ClassNotFoundException, NoSuchFieldException {
        return ofUniversalClass(className, (universalClass, mappingType) ->
                universalClass.getField(mappingType, fieldName));
    }

    @StableAPI.Expose
    public static UniversalField getField(FieldInsnNode fieldInsnNode)
            throws ClassNotFoundException, NoSuchFieldException {
        return getField(fieldInsnNode.owner, fieldInsnNode.name);
    }

    @StableAPI.Expose
    public static UniversalField getField(String className, FieldNode fieldNode)
            throws ClassNotFoundException, NoSuchFieldException {
        return getField(className, fieldNode.name);
    }

    @StableAPI.Expose
    public static UniversalMethod getMethod(String className, String methodName, String methodDesc)
            throws ClassNotFoundException, NoSuchMethodException {
        return ofUniversalClass(className, (universalClass, mappingType) ->
                universalClass.getMethod(mappingType, methodName, methodDesc));
    }

    @StableAPI.Expose
    public static UniversalMethod getMethod(MethodInsnNode methodInsnNode)
            throws ClassNotFoundException, NoSuchMethodException {
        return getMethod(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);
    }

    @StableAPI.Expose
    public static UniversalMethod getMethod(String className, MethodNode methodNode)
            throws ClassNotFoundException, NoSuchMethodException {
        return getMethod(className, methodNode.name, methodNode.desc);
    }

    @StableAPI.Expose
    public static UniversalClass classForName(NameType nameType, MappingType mappingType, String className)
            throws ClassNotFoundException {
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
            throw new ClassNotFoundException(
                    "Could not find class \"" + className + "\" with " + nameType.name().toLowerCase() +
                    " name in the " + mappingType.name() + " mapping.");
        }
    }

    //TODO: Should this be public and exposed instead?
    private static <T, E extends Throwable> T ofUniversalClass(
            String className, ThrowingBIFunction<UniversalClass, MappingType, T, E> elementGetter)
            throws ClassNotFoundException, E {
        UniversalClass universalClass;
        MappingType mappingType;
        if (!CoreLoadingPlugin.isObfuscated()) {
            try {
                mappingType = MappingType.MCP;
                universalClass = classForName(NameType.Internal, MappingType.MCP, className);

            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("Could not find the class " + className +
                                                 " in the MCP mappings. Are you sure it's a Minecraft class? (we're in dev, cannot use SRG or Notch here).");
            }
        } else {
            try {
                mappingType = MappingType.SRG;
                universalClass = classForName(NameType.Internal, mappingType, className);
            } catch (ClassNotFoundException e) {
                try {
                    mappingType = MappingType.Notch;
                    universalClass = classForName(NameType.Internal, mappingType, className);
                } catch (ClassNotFoundException ex) {
                    throw new ClassNotFoundException("Could not find the class " + className +
                                                     " neither in the SRG nor in the Notch mappings. Are you sure it's a Minecraft class? (we're in obf, cannot use MCP here)");
                }
            }
        }

        return elementGetter.apply(universalClass, mappingType);
    }

    @StableAPI.Expose
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
}
