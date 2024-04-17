/*
 * Copyright (C) 2022-2023 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
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
import com.falsepattern.lib.internal.FPLog;
import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.internal.Share;
import com.falsepattern.lib.internal.asm.CoreLoadingPlugin;
import com.falsepattern.lib.mapping.storage.Lookup;
import com.falsepattern.lib.mapping.types.MappingType;
import com.falsepattern.lib.mapping.types.NameType;
import com.falsepattern.lib.mapping.types.UniversalClass;
import com.falsepattern.lib.mapping.types.UniversalField;
import com.falsepattern.lib.mapping.types.UniversalMethod;
import com.falsepattern.lib.util.ResourceUtil;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@StableAPI(since = "0.10.0")
public class MappingManager {
    private static Lookup<UniversalClass> internalLookup;
    private static Lookup<UniversalClass> regularLookup;
    private static boolean initialized = false;
    private static final Object MUTEX = new Object();
    private static final AtomicLong lastInitializedAt = new AtomicLong();

    private static class CleanupThread extends Thread {
        public CleanupThread() {
            setName("MappingManager Cleanup Watchdog");
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                synchronized (MUTEX) {
                    long time = System.currentTimeMillis();
                    long lastInit = lastInitializedAt.get();
                    if (time - lastInit < 10_000) {
                        continue;
                    }
                    FPLog.LOG.info("Terminating MappingManager");
                    internalLookup = null;
                    regularLookup = null;
                    initialized = false;
                    return;
                }
            }
        }
    }

    @SneakyThrows
    public static void initialize() {
        synchronized (MUTEX) {
            if (initialized) {
                lastInitializedAt.set(System.currentTimeMillis());
                return;
            }
            initialized = true;
            val cleanupThread = new CleanupThread();
            FPLog.LOG.info("Initializing MappingManager");
            internalLookup = new Lookup<>();
            regularLookup = new Lookup<>();
            val stringPool = new HashMap<String, String>();
            val classMappings =
                    new String(ResourceUtil.getResourceBytesFromJar("/classes.csv", CoreLoadingPlugin.class)).split("\n");
            for (int i = 1; i < classMappings.length; i++) {
                val line = classMappings[i].split(",");
                val clazz = new UniversalClass(line, stringPool);
                internalLookup.unwrap(clazz.internalName, clazz);
                regularLookup.unwrap(clazz.regularName, clazz);
            }
            var fieldMappings =
                    new String(ResourceUtil.getResourceBytesFromJar("/fields.csv", CoreLoadingPlugin.class)).split("\n");
            for (int i = 1; i < fieldMappings.length; i++) {
                val line = fieldMappings[i].split(",");
                val clazz = internalLookup.get(MappingType.Notch, line[0].substring(0, line[0].lastIndexOf('/')));
                UniversalField.createAndAddToParent(clazz, line, stringPool);
            }
            val methodMappings =
                    new String(ResourceUtil.getResourceBytesFromJar("/methods.csv", CoreLoadingPlugin.class)).split("\n");
            for (int i = 1; i < methodMappings.length; i++) {
                val line = methodMappings[i].split(",");
                val clazz = internalLookup.get(MappingType.Notch, line[0].substring(0, line[0].lastIndexOf('/')));
                UniversalMethod.createAndAddToParent(clazz, line, stringPool);
            }
            lastInitializedAt.set(System.currentTimeMillis());
            cleanupThread.start();
        }
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
            throw new ClassNotFoundException("Could not find class \""
                                             + className
                                             + "\" with "
                                             + nameType.name()
                                                       .toLowerCase()
                                             + " name in the "
                                             + mappingType.name()
                                             + " mapping.");
        }
    }

    @StableAPI.Expose
    public static boolean containsClass(NameType nameType, MappingType mappingType, String className) {
        initialize();
        switch (nameType) {
            case Internal:
                return internalLookup.containsKey(mappingType, className);
            case Regular:
                return regularLookup.containsKey(mappingType, className);
            default:
                throw new IllegalArgumentException("Invalid enum value " + nameType);
        }
    }

    @StableAPI.Expose
    public static UniversalField getField(FieldInsnNode instruction)
            throws ClassNotFoundException, NoSuchFieldException {
        initialize();
        if (!CoreLoadingPlugin.isObfuscated()) {
            try {
                return classForName(NameType.Internal, MappingType.MCP, instruction.owner).getField(MappingType.MCP,
                                                                                                    instruction.name);
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("Could not find the class "
                                                 + instruction.owner
                                                 + " in the MCP mappings. Are you sure it's a Minecraft class? (we're in dev, cannot use SRG or Notch here).");
            }
        } else {
            try {
                return classForName(NameType.Internal, MappingType.SRG, instruction.owner).getField(MappingType.SRG,
                                                                                                    instruction.name);
            } catch (ClassNotFoundException e) {
                try {
                    return classForName(NameType.Internal,
                                        MappingType.Notch,
                                        instruction.owner).getField(MappingType.Notch, instruction.name);
                } catch (ClassNotFoundException ex) {
                    throw new ClassNotFoundException("Could not find the class "
                                                     + instruction.owner
                                                     + " neither in the SRG nor in the Notch mappings. Are you sure it's a Minecraft class? (we're in obf, cannot use MCP here)");
                }
            }
        }
    }

    @StableAPI.Expose
    public static UniversalMethod getMethod(MethodInsnNode instruction)
            throws ClassNotFoundException, NoSuchMethodException {
        initialize();
        if (!CoreLoadingPlugin.isObfuscated()) {
            try {
                return classForName(NameType.Internal, MappingType.MCP, instruction.owner).getMethod(MappingType.MCP,
                                                                                                     instruction.name,
                                                                                                     instruction.desc);
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("Could not find the class "
                                                 + instruction.owner
                                                 + " in the MCP mappings. Are you sure it's a Minecraft class? (we're in dev, cannot use SRG or Notch here).");
            }
        } else {
            try {
                return classForName(NameType.Internal, MappingType.SRG, instruction.owner).getMethod(MappingType.SRG,
                                                                                                     instruction.name,
                                                                                                     instruction.desc);
            } catch (ClassNotFoundException e) {
                try {
                    return classForName(NameType.Internal,
                                        MappingType.Notch,
                                        instruction.owner).getMethod(MappingType.Notch,
                                                                     instruction.name,
                                                                     instruction.desc);
                } catch (ClassNotFoundException ex) {
                    throw new ClassNotFoundException("Could not find the class "
                                                     + instruction.owner
                                                     + " neither in the SRG nor in the Notch mappings. Are you sure it's a Minecraft class? (we're in obf, cannot use MCP here)");
                }
            }
        }
    }
}
