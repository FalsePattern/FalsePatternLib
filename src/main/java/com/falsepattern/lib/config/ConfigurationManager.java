package com.falsepattern.lib.config;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.CoreLoadingPlugin;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Class for controlling the loading of configuration files.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@StableAPI(since = "0.6.0")
public class ConfigurationManager {
    private static final Map<String, Set<Class<?>>> configs = new HashMap<>();

    private static final ConfigurationManager instance = new ConfigurationManager();

    private static boolean initialized = false;

    private static Path configDir;

    /**
     * Registers a configuration class to be loaded.
     * @param config The class to register.
     */
    public static void registerConfig(Class<?> config) throws IllegalAccessException {
        val cfg = Optional.ofNullable(config.getAnnotation(Config.class)).orElseThrow(() -> new IllegalArgumentException("Class " + config.getName() + " does not have a @Config annotation!"));
        val cfgSet = configs.computeIfAbsent(cfg.modid(), (ignored) -> new HashSet<>());
        cfgSet.add(config);
        processConfig(config);
    }

    /**
     * Internal, do not use.
     */
    public static void init() {
        if (initialized) return;
        configDir = CoreLoadingPlugin.mcDir.toPath().resolve("config");
        MinecraftForge.EVENT_BUS.register(instance);
        initialized = true;
    }

    /**
     * Internal, do not use.
     * @param event The event.
     */
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        val configClasses = configs.get(event.modID);
        if (configClasses == null)
            return;
        configClasses.forEach((config) -> {
            try {
                processConfig(config);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void processConfig(Class<?> configClass) throws IllegalAccessException {
        val cfg = configClass.getAnnotation(Config.class);
        val category = cfg.category();
        var configName = cfg.name().trim();
        if (configName.length() == 0) {
            configName = cfg.modid();
        }
        val rawConfig = new Configuration(configDir.resolve(configName + ".cfg").toFile());
        rawConfig.load();
        val cat = rawConfig.getCategory(category);
        for (val field: configClass.getDeclaredFields()) {
            if (field.getAnnotation(Config.Ignore.class) != null) continue;
            field.setAccessible(true);
            val comment = Optional.ofNullable(field.getAnnotation(Config.Comment.class)).map(Config.Comment::value).map((lines) -> String.join("\n", lines)).orElse("");
            val name = Optional.ofNullable(field.getAnnotation(Config.Name.class)).map(Config.Name::value).orElse(field.getName());
            val langKey = Optional.ofNullable(field.getAnnotation(Config.LangKey.class)).map(Config.LangKey::value).orElse(name);
            var boxed = false;
            if ((boxed = field.getType().equals(Boolean.class)) || field.getType().equals(boolean.class)) {
                val defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultBoolean.class)).map(Config.DefaultBoolean::value).orElse(boxed ? (Boolean) field.get(null) : field.getBoolean(null));
                field.setBoolean(null, rawConfig.getBoolean(name, category, defaultValue, comment, langKey));
            } else if ((boxed = field.getType().equals(Integer.class)) || field.getType().equals(int.class)) {
                val range = Optional.ofNullable(field.getAnnotation(Config.RangeInt.class));
                val min = range.map(Config.RangeInt::min).orElse(Integer.MIN_VALUE);
                val max = range.map(Config.RangeInt::max).orElse(Integer.MAX_VALUE);
                val defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultInt.class)).map(Config.DefaultInt::value).orElse(boxed ? (Integer)field.get(null) : field.getInt(null));
                field.setInt(null, rawConfig.getInt(name, category, defaultValue, min, max, comment, langKey));
            } else if ((boxed = field.getType().equals(Float.class)) || field.getType().equals(float.class)) {
                val range = Optional.ofNullable(field.getAnnotation(Config.RangeFloat.class));
                val min = range.map(Config.RangeFloat::min).orElse(Float.MIN_VALUE);
                val max = range.map(Config.RangeFloat::max).orElse(Float.MAX_VALUE);
                val defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultFloat.class)).map(Config.DefaultFloat::value).orElse(boxed ? (Float) field.get(null) : field.getFloat(null));
                field.setDouble(null, rawConfig.getFloat(name, category, defaultValue, min, max, comment, langKey));
            } else if (field.getType().equals(String.class)) {
                val defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultString.class)).map(Config.DefaultString::value).orElse((String)field.get(null));
                val pattern = Optional.ofNullable(field.getAnnotation(Config.Pattern.class)).map(Config.Pattern::value).map(Pattern::compile).orElse(null);
                field.set(null, rawConfig.getString(name, category, defaultValue, comment, langKey, pattern));
            }
            if (field.isAnnotationPresent(Config.RequiresMcRestart.class)) {
                cat.setRequiresMcRestart(true);
            }
            if (field.isAnnotationPresent(Config.RequiresWorldRestart.class)) {
                cat.setRequiresWorldRestart(true);
            }
        }
        rawConfig.save();
    }
}
