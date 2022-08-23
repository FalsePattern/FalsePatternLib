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
package com.falsepattern.lib.config;

import com.falsepattern.lib.DeprecationDetails;
import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.ReflectionUtil;
import com.falsepattern.lib.internal.Share;
import com.falsepattern.lib.internal.impl.config.ConfigurationManagerImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import cpw.mods.fml.client.config.IConfigElement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Class for controlling the loading of configuration files.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@StableAPI(since = "0.6.0")
public class ConfigurationManager {

    @StableAPI.Expose(since = "0.10.0")
    public static void initialize(Class<?>... configClasses) throws ConfigException {
        initialize((a, b) -> {}, configClasses);
    }

    /**
     * You can use this method in the static initializer of a config class to self-load it without extra effort.
     */
    @StableAPI.Expose(since = "0.10.3")
    @SneakyThrows
    public static void selfInit() {
        initialize(ReflectionUtil.getCallerClass());
    }

    @StableAPI.Expose(since = "0.10.0")
    public static void initialize(BiConsumer<Class<?>, Field> validatorErrorCallback, Class<?>... configClasses)
            throws ConfigException {
        for (val clazz : configClasses) {
            ConfigurationManagerImpl.register(clazz);
            ConfigurationManagerImpl.load(clazz);
            ConfigurationManagerImpl.validateFields(validatorErrorCallback, clazz, true);
            ConfigurationManagerImpl.save(clazz);
        }
    }

    @StableAPI.Expose(since = "0.10.0")
    public static boolean validate(boolean resetInvalid, Class<?>... configClasses) throws ConfigException {
        return validate((x, y) -> {}, resetInvalid, configClasses);
    }

    @StableAPI.Expose(since = "0.10.0")
    public static boolean validate(BiConsumer<Class<?>, Field> validatorErrorCallback, boolean resetInvalid, Class<?>... configClasses)
            throws ConfigException {
        boolean valid = true;
        for (val clazz : configClasses) {
            valid &= ConfigurationManagerImpl.validateFields(validatorErrorCallback, clazz, resetInvalid);
        }
        return valid;
    }

    @StableAPI.Expose(since = "0.10.0")
    public static void loadFromFile(Class<?>... configClasses) throws ConfigException {
        for (val clazz : configClasses) {
            ConfigurationManagerImpl.load(clazz);
        }
    }

    @StableAPI.Expose(since = "0.10.0")
    public static void saveToFile(boolean validateAndResetInvalid, Class<?>... configClasses) throws ConfigException {
        for (val clazz : configClasses) {
            if (validateAndResetInvalid) {
                ConfigurationManagerImpl.validateFields((a, b) -> {}, clazz, true);
            }
            ConfigurationManagerImpl.save(clazz);
        }
    }

    /**
     * Registers a configuration class to be loaded. This should be done early on.
     *
     * @param configClass The class to register.
     */
    @Deprecated
    @DeprecationDetails(deprecatedSince = "0.10.0")
    @StableAPI.Expose
    public static void registerConfig(Class<?> configClass) throws ConfigException {
        Share.LOG
                       .warn("A mod is using the deprecated config registration method! The following exception contains the stacktrace.",
                             new Exception());
        ConfigurationManagerImpl.registerLoadSaveConfig(configClass);
    }

    /**
     * Process the configuration into a list of config elements usable in config GUI code.
     *
     * @param configClass The class to process.
     *
     * @return The configuration elements.
     */
    @SuppressWarnings("rawtypes")
    @StableAPI.Expose
    public static List<IConfigElement> getConfigElements(Class<?> configClass) throws ConfigException {
        return ConfigurationManagerImpl.getConfigElements(configClass);
    }

    /**
     * Same as {@link #getConfigElements(Class)}, but for multiple config classes at once.
     *
     * @param configClasses The classes to process.
     *
     * @return The configuration elements.
     */
    @SuppressWarnings("rawtypes")
    @StableAPI.Expose(since = "0.10.0")
    public static List<IConfigElement> getConfigElementsMulti(Class<?>... configClasses) throws ConfigException {
        switch (configClasses.length) {
            case 0:
                return Collections.emptyList();
            case 1:
                return getConfigElements(configClasses[0]);
            default:
                val result = new ArrayList<IConfigElement>();
                for (val configClass : configClasses) {
                    result.addAll(getConfigElements(configClass));
                }
                return result;
        }
    }
}
