package com.falsepattern.lib.internal.impl.config;

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.IConfigElementProxy;
import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.util.FileUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The actual implementation of ConfigurationManager. Migrated stuff here so that we don't unnecessarily expose
 * internal-use functionality.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigurationManagerImpl {
    private static final Map<String, Configuration> configs = new HashMap<>();
    private static final Map<Configuration, Set<Class<?>>> configToClassMap = new HashMap<>();
    private static final ConfigurationManagerImpl instance = new ConfigurationManagerImpl();
    private static boolean initialized = false;
    private static Path configDir;

    public static void registerConfig(Class<?> configClass) throws ConfigException {
        init();
        val cfg = Optional.ofNullable(configClass.getAnnotation(Config.class))
                          .orElseThrow(() -> new ConfigException(
                                  "Class " + configClass.getName() + " does not have a @Config annotation!"));
        val category = Optional.of(cfg.category().trim())
                               .map((cat) -> cat.length() == 0 ? null : cat)
                               .orElseThrow(() -> new ConfigException(
                                       "Config class " + configClass.getName() + " has an empty category!"));
        val rawConfig = configs.computeIfAbsent(cfg.modid(), (ignored) -> {
            val c = new Configuration(configDir.resolve(cfg.modid() + ".cfg").toFile());
            c.load();
            return c;
        });
        configToClassMap.computeIfAbsent(rawConfig, (ignored) -> new HashSet<>()).add(configClass);
        try {
            processConfigInternal(configClass, category, rawConfig);
            rawConfig.save();
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | NoSuchFieldException e) {
            throw new ConfigException(e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<IConfigElement> getConfigElements(Class<?> configClass) throws ConfigException {
        init();
        val cfg = Optional.ofNullable(configClass.getAnnotation(Config.class))
                          .orElseThrow(() -> new ConfigException(
                                  "Class " + configClass.getName() + " does not have a @Config annotation!"));
        val rawConfig = Optional.ofNullable(configs.get(cfg.modid()))
                                .map((conf) -> Optional.ofNullable(configToClassMap.get(conf))
                                                       .map((l) -> l.contains(configClass))
                                                       .orElse(false) ? conf : null)
                                .orElseThrow(() -> new ConfigException(
                                        "Tried to get config elements for non-registered config class!"));
        val category = cfg.category();
        val elements = new ConfigElement<>(rawConfig.getCategory(category)).getChildElements();
        return elements.stream().map((element) -> new IConfigElementProxy(element, () -> {
            try {
                processConfigInternal(configClass, category, rawConfig);
                rawConfig.save();
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | NoSuchFieldException |
                     ConfigException e) {
                e.printStackTrace();
            }
        })).collect(Collectors.toList());
    }

    private static void processConfigInternal(Class<?> configClass, String category, Configuration rawConfig)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException,
            ConfigException {
        val cat = rawConfig.getCategory(category);
        for (val field : configClass.getDeclaredFields()) {
            if (field.getAnnotation(Config.Ignore.class) != null) {
                continue;
            }
            field.setAccessible(true);
            val comment = Optional.ofNullable(field.getAnnotation(Config.Comment.class))
                                  .map(Config.Comment::value)
                                  .map((lines) -> String.join("\n", lines))
                                  .orElse("");
            val name = Optional.ofNullable(field.getAnnotation(Config.Name.class))
                               .map(Config.Name::value)
                               .orElse(field.getName());
            val langKey = Optional.ofNullable(field.getAnnotation(Config.LangKey.class))
                                  .map(Config.LangKey::value)
                                  .orElse(name);
            val fieldClass = field.getType();
            var boxed = false;
            if ((boxed = fieldClass.equals(Boolean.class)) || fieldClass.equals(boolean.class)) {
                val defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultBoolean.class))
                                           .map(Config.DefaultBoolean::value)
                                           .orElse(boxed ? (Boolean) field.get(null) : field.getBoolean(null));
                field.setBoolean(null, rawConfig.getBoolean(name, category, defaultValue, comment, langKey));
            } else if ((boxed = fieldClass.equals(Integer.class)) || fieldClass.equals(int.class)) {
                val range = Optional.ofNullable(field.getAnnotation(Config.RangeInt.class));
                val min = range.map(Config.RangeInt::min).orElse(Integer.MIN_VALUE);
                val max = range.map(Config.RangeInt::max).orElse(Integer.MAX_VALUE);
                val defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultInt.class))
                                           .map(Config.DefaultInt::value)
                                           .orElse(boxed ? (Integer) field.get(null) : field.getInt(null));
                field.setInt(null, rawConfig.getInt(name, category, defaultValue, min, max, comment, langKey));
            } else if ((boxed = fieldClass.equals(Float.class)) || fieldClass.equals(float.class)) {
                val range = Optional.ofNullable(field.getAnnotation(Config.RangeFloat.class));
                val min = range.map(Config.RangeFloat::min).orElse(Float.MIN_VALUE);
                val max = range.map(Config.RangeFloat::max).orElse(Float.MAX_VALUE);
                val defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultFloat.class))
                                           .map(Config.DefaultFloat::value)
                                           .orElse(boxed ? (Float) field.get(null) : field.getFloat(null));
                field.setFloat(null, rawConfig.getFloat(name, category, defaultValue, min, max, comment, langKey));
            } else if (fieldClass.equals(String.class)) {
                val defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultString.class))
                                           .map(Config.DefaultString::value)
                                           .orElse((String) field.get(null));
                val pattern = Optional.ofNullable(field.getAnnotation(Config.Pattern.class))
                                      .map(Config.Pattern::value)
                                      .map(Pattern::compile)
                                      .orElse(null);
                field.set(null, rawConfig.getString(name, category, defaultValue, comment, langKey, pattern));
            } else if (fieldClass.isEnum()) {
                val enumValues = Arrays.stream((Object[]) fieldClass.getDeclaredMethod("values").invoke(null))
                                       .map((obj) -> (Enum<?>) obj)
                                       .collect(Collectors.toList());
                val defaultValue = (Enum<?>) Optional.ofNullable(field.getAnnotation(Config.DefaultEnum.class))
                                                     .map(Config.DefaultEnum::value)
                                                     .map((defName) -> extractField(fieldClass, defName))
                                                     .map(ConfigurationManagerImpl::extractValue)
                                                     .orElse(field.get(null));
                val possibleValues = enumValues.stream().map(Enum::name).toArray(String[]::new);
                var value = rawConfig.getString(name, category, defaultValue.name(),
                                                comment + "\nPossible values: " + Arrays.toString(possibleValues) +
                                                "\n", possibleValues, langKey);

                try {
                    if (!Arrays.asList(possibleValues).contains(value)) {
                        throw new NoSuchFieldException();
                    }
                    val enumField = fieldClass.getDeclaredField(value);
                    if (!enumField.isEnumConstant()) {
                        throw new NoSuchFieldException();
                    }
                    field.set(null, enumField.get(null));
                } catch (NoSuchFieldException e) {
                    FalsePatternLib.getLog()
                                   .warn("Invalid value " + value + " for enum configuration field " + field.getName() +
                                         " of type " + fieldClass.getName() + " in config class " +
                                         configClass.getName() + "! Using default value of " + defaultValue + "!");
                    field.set(null, defaultValue);
                }
            } else if (fieldClass.isArray() && fieldClass.getComponentType().equals(String.class)) {
                val defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultStringList.class))
                                           .map(Config.DefaultStringList::value)
                                           .orElse((String[]) field.get(null));
                var value = rawConfig.getStringList(name, category, defaultValue, comment, null, langKey);
                field.set(null, value);
            } else {
                throw new ConfigException("Illegal config field: " + field.getName() + " in " + configClass.getName() +
                                          ": Unsupported type " + fieldClass.getName() +
                                          "! Did you forget an @Ignore annotation?");
            }
            if (field.isAnnotationPresent(Config.RequiresMcRestart.class)) {
                cat.setRequiresMcRestart(true);
            }
            if (field.isAnnotationPresent(Config.RequiresWorldRestart.class)) {
                cat.setRequiresWorldRestart(true);
            }
        }
    }

    @SneakyThrows
    private static Field extractField(Class<?> clazz, String field) {
        return clazz.getDeclaredField(field);
    }

    @SneakyThrows
    private static Object extractValue(Field field) {
        return field.get(null);
    }

    private static void init() {
        if (initialized) {
            return;
        }
        configDir = FileUtil.getMinecraftHome().toPath().resolve("config");
        initialized = true;
    }

    public static void registerBus() {
        FMLCommonHandler.instance().bus().register(instance);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        init();
        val config = configs.get(event.modID);
        if (config == null) {
            return;
        }
        val configClasses = configToClassMap.get(config);
        configClasses.forEach((configClass) -> {
            try {
                val category = Optional.ofNullable(configClass.getAnnotation(Config.class))
                                       .map(Config::category)
                                       .orElseThrow(() -> new ConfigException(
                                               "Failed to get config category for class " + configClass.getName()));
                processConfigInternal(configClass, category, config);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
