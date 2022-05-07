package com.falsepattern.lib.config;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.CoreLoadingPlugin;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lombok.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    public static void registerConfig(Class<?> config) throws ConfigException {
        val cfg = Optional.ofNullable(config.getAnnotation(Config.class)).orElseThrow(() -> new ConfigException("Class " + config.getName() + " does not have a @Config annotation!"));
        val cfgSet = configs.computeIfAbsent(cfg.modid(), (ignored) -> new HashSet<>());
        cfgSet.add(config);
        try {
            processConfig(config);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | NoSuchFieldException e) {
            throw new ConfigException(e);
        }
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
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void processConfig(Class<?> configClass) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException, ConfigException {
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
            val fieldClass = field.getType();
            var boxed = false;
            if ((boxed = fieldClass.equals(Boolean.class)) || fieldClass.equals(boolean.class)) {
                val defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultBoolean.class)).map(Config.DefaultBoolean::value).orElse(boxed ? (Boolean) field.get(null) : field.getBoolean(null));
                field.setBoolean(null, rawConfig.getBoolean(name, category, defaultValue, comment, langKey));
            } else if ((boxed = fieldClass.equals(Integer.class)) || fieldClass.equals(int.class)) {
                val range = Optional.ofNullable(field.getAnnotation(Config.RangeInt.class));
                val min = range.map(Config.RangeInt::min).orElse(Integer.MIN_VALUE);
                val max = range.map(Config.RangeInt::max).orElse(Integer.MAX_VALUE);
                val defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultInt.class)).map(Config.DefaultInt::value).orElse(boxed ? (Integer)field.get(null) : field.getInt(null));
                field.setInt(null, rawConfig.getInt(name, category, defaultValue, min, max, comment, langKey));
            } else if ((boxed = fieldClass.equals(Float.class)) || fieldClass.equals(float.class)) {
                val range = Optional.ofNullable(field.getAnnotation(Config.RangeFloat.class));
                val min = range.map(Config.RangeFloat::min).orElse(Float.MIN_VALUE);
                val max = range.map(Config.RangeFloat::max).orElse(Float.MAX_VALUE);
                val defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultFloat.class)).map(Config.DefaultFloat::value).orElse(boxed ? (Float) field.get(null) : field.getFloat(null));
                field.setDouble(null, rawConfig.getFloat(name, category, defaultValue, min, max, comment, langKey));
            } else if (fieldClass.equals(String.class)) {
                val defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultString.class)).map(Config.DefaultString::value).orElse((String)field.get(null));
                val pattern = Optional.ofNullable(field.getAnnotation(Config.Pattern.class)).map(Config.Pattern::value).map(Pattern::compile).orElse(null);
                field.set(null, rawConfig.getString(name, category, defaultValue, comment, langKey, pattern));
            } else if (fieldClass.isEnum()) {
                val enumValues = Arrays.stream((Object[])fieldClass.getDeclaredMethod("values").invoke(null)).map((obj) -> (Enum<?>)obj).collect(Collectors.toList());
                val defaultValue = (Enum<?>)
                        Optional.ofNullable(field.getAnnotation(Config.DefaultEnum.class))
                                .map(Config.DefaultEnum::value)
                                .map((defName) -> extractField(fieldClass, defName))
                                .map(ConfigurationManager::extractValue)
                                .orElse(field.get(null));
                val possibleValues = enumValues.stream().map(Enum::name).toArray(String[]::new);
                field.set(null, fieldClass.getDeclaredField(rawConfig.getString(name, category, defaultValue.name(), comment, possibleValues, langKey)));
            } else {
                throw new ConfigException("Illegal config field: " + field.getName() + " in " + configClass.getName() + ": Unsupported type " + fieldClass.getName() + "! Did you forget an @Ignore annotation?");
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

    @SneakyThrows
    private static Field extractField(Class<?> clazz, String field) {
        return clazz.getDeclaredField(field);
    }

    @SneakyThrows
    private static Object extractValue(Field field) {
        return field.get(null);
    }
}
