/*
 * This file is part of FalsePatternLib.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalsePatternLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalsePatternLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalsePatternLib. If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.lib.internal.impl.config;

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.event.AllConfigSyncEvent;
import com.falsepattern.lib.config.event.ConfigSyncEvent;
import com.falsepattern.lib.internal.FPLog;
import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.internal.impl.config.net.SyncRequest;
import com.falsepattern.lib.util.FileUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.client.event.ConfigChangedEvent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * The actual implementation of ConfigurationManager. Migrated stuff here so that we don't unnecessarily expose
 * internal-use functionality.
 * <p>
 * Do not read if you value your sanity.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationManagerImpl {
    private static final Map<String, Configuration> configs = new HashMap<>();
    private static final Map<Configuration, Set<Class<?>>> configToClassMap = new HashMap<>();
    private static final Map<Class<?>, ParsedConfiguration> parsedConfigMap = new HashMap<>();
    private static final BiMap<String, Class<?>> serializedNames = HashBiMap.create();
    private static boolean initialized = false;
    private static Path configDir;

    public static void register(Class<?> configClass) throws ConfigException {
        init();
        if (!parsedConfigMap.containsKey(configClass)) {
            val parsedConfig = ParsedConfiguration.parseConfig(configClass);
            configToClassMap.computeIfAbsent(parsedConfig.rawConfig, (ignored) -> new HashSet<>()).add(configClass);
            parsedConfigMap.put(configClass, parsedConfig);
            serializedNames.put(parsedConfig.modid + "$" + parsedConfig.category, configClass);
        }
    }

    static Configuration getForgeConfig(String modid) {
        return configs.computeIfAbsent(modid, (ignored) -> {
            val c = new Configuration(configDir.resolve(modid + ".cfg").toFile());
            c.load();
            return c;
        });
    }

    public static void load(Class<?> configClass) throws ConfigException {
        if (!parsedConfigMap.containsKey(configClass)) {
            throw new ConfigException("Class " + configClass.getName() + " is not a registered configuration!");
        }
        parsedConfigMap.get(configClass).load();
    }

    public static void save(Class<?> configClass) throws ConfigException {
        if (!parsedConfigMap.containsKey(configClass)) {
            throw new ConfigException("Class " + configClass.getName() + " is not a registered configuration!");
        }
        parsedConfigMap.get(configClass).save();
    }

    public static boolean validateFields(BiConsumer<Class<?>, Field> invalidFieldHandler, Class<?> configClass, boolean resetInvalid)
            throws ConfigException {
        if (!parsedConfigMap.containsKey(configClass)) {
            throw new ConfigException("Class " + configClass.getName() + " is not a registered configuration!");
        }
        val parsed = parsedConfigMap.get(configClass);
        return parsed.validate(invalidFieldHandler, resetInvalid);
    }

    public static void sendRequest(DataOutput output) throws IOException {
        val synced = new ArrayList<Class<?>>();
        val inv = serializedNames.inverse();
        for (val entry : parsedConfigMap.entrySet()) {
            if (entry.getValue().sync) {
                synced.add(entry.getKey());
            }
        }
        output.writeInt(synced.size());
        for (val clazz : synced) {
            output.writeUTF(inv.get(clazz));
        }
    }

    public static List<Class<?>> receiveRequest(DataInput input) throws IOException {
        val result = new ArrayList<Class<?>>();
        val count = input.readInt();
        val requestedNames = new HashSet<String>();
        for (int i = 0; i < count; i++) {
            requestedNames.add(input.readUTF());
        }
        for (val entry : serializedNames.keySet()) {
            if (requestedNames.contains(entry)) {
                result.add(serializedNames.get(entry));
            }
        }
        return result;
    }

    public static void sendReply(DataOutput output, List<Class<?>> requestedClasses) throws IOException {
        val syncEntries = new HashMap<>(parsedConfigMap);
        for (val entry : parsedConfigMap.entrySet()) {
            if (!entry.getValue().sync || !requestedClasses.contains(entry.getKey())) {
                syncEntries.remove(entry.getKey());
            }
        }
        output.writeInt(syncEntries.size());
        val inv = serializedNames.inverse();
        for (val entry : syncEntries.entrySet()) {
            output.writeUTF(inv.get(entry.getKey()));
            val b = new ByteArrayOutputStream();
            val bo = new DataOutputStream(b);
            entry.getValue().transmit(bo);
            bo.close();
            val bytes = b.toByteArray();
            output.writeInt(bytes.length);
            output.write(bytes);
        }
    }

    public static void receiveReply(DataInput input) throws IOException {
        if (AllConfigSyncEvent.postStart()) {
            FPLog.LOG.warn("All config synchronization was cancelled by event.");
            return;
        }
        int count = input.readInt();
        for (int i = 0; i < count; i++) {
            String serializedName = input.readUTF();
            int dataSize = input.readInt();
            val opt = serializedNames.keySet().stream().filter((key) -> key.equals(serializedName)).findFirst();
            if (!opt.isPresent()) {
                input.skipBytes(dataSize);
                FPLog.LOG.warn("Server tried to sync config not registered on our side: " + serializedName);
                continue;
            }
            val clazz = serializedNames.get(opt.get());
            val config = parsedConfigMap.get(clazz);
            if (!config.sync) {
                input.skipBytes(dataSize);
                FPLog.LOG.warn("Server tried to sync config without @Synchronize annotation on our side: "
                               + serializedName);
                continue;
            }
            if (ConfigSyncEvent.postStart(clazz)) {
                input.skipBytes(dataSize);
                FPLog.LOG.warn("Config synchronization was cancelled by event for: " + serializedName);
                continue;
            }
            val bytes = new byte[dataSize];
            input.readFully(bytes);
            val b = new ByteArrayInputStream(bytes);
            val bi = new DataInputStream(b);
            try {
                config.receive(bi);
                config.validate((x, y) -> {}, true);
                ConfigSyncEvent.postEndSuccess(clazz);
            } catch (Throwable e) {
                ConfigSyncEvent.postEndFailure(clazz, e);
            }
            bi.close();
        }
        AllConfigSyncEvent.postEnd();
    }

    public static void loadRawConfig(Configuration rawConfig) throws ConfigException {
        rawConfig.load();
        for (val configClass : configToClassMap.get(rawConfig)) {
            val config = parsedConfigMap.get(configClass);
            try {
                config.reloadFields();
            } catch (IllegalAccessException e) {
                throw new ConfigException(e);
            }
        }
    }

    @SuppressWarnings({"rawtypes"})
    public static List<IConfigElement> getConfigElements(Class<?> configClass) throws ConfigException {
        if (!parsedConfigMap.containsKey(configClass)) {
            throw new ConfigException("Class " + configClass.getName() + " is not a registered configuration!");
        }
        val config = parsedConfigMap.get(configClass);
        return config.getConfigElements();
    }

    private static void init() {
        if (initialized) {
            return;
        }
        configDir = FileUtil.getMinecraftHome().toPath().resolve("config");
        initialized = true;
    }

    public static void sendSyncRequest() throws IOException {
        val event = new SyncRequest();
        event.transmit();
        FalsePatternLib.NETWORK.sendToServer(event);
    }

    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        init();
        val rawConfig = configs.get(event.modID);
        if (rawConfig == null) {
            return;
        }
        val configClasses = configToClassMap.get(rawConfig);
        for (val clazz : configClasses) {
            val config = parsedConfigMap.get(clazz);
            config.configChanged();
        }
    }
}
