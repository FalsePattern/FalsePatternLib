package com.falsepattern.lib.config;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.impl.config.ConfigurationManagerImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import cpw.mods.fml.client.config.IConfigElement;

import java.util.List;

/**
 * Class for controlling the loading of configuration files.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@StableAPI(since = "0.6.0")
public class ConfigurationManager {

    /**
     * Registers a configuration class to be loaded. This should be done in preInit.
     *
     * @param configClass The class to register.
     */
    public static void registerConfig(Class<?> configClass) throws ConfigException {
        ConfigurationManagerImpl.registerConfig(configClass);
    }

    /**
     * Process the configuration into a list of config elements usable in config GUI code.
     *
     * @param configClass The class to process.
     *
     * @return The configuration elements.
     */
    @SuppressWarnings("rawtypes")
    public static List<IConfigElement> getConfigElements(Class<?> configClass) throws ConfigException {
        return ConfigurationManagerImpl.getConfigElements(configClass);
    }
}
