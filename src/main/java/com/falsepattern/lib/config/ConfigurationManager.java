package com.falsepattern.lib.config;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.CoreLoadingPlugin;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.nio.file.Path;
import java.util.*;

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
        val cfg = Optional.of(config.getAnnotation(Config.class)).orElseThrow(() -> new IllegalArgumentException("Class " + config.getName() + " does not have a @Config annotation!"));
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
        val cat = rawConfig.getCategory(category);

        for (val field: configClass.getDeclaredFields()) {
            if (field.getAnnotation(Config.Ignore.class) != null) continue;
            field.setAccessible(true);
            val comment = Optional.of(field.getAnnotation(Config.Comment.class)).map(Config.Comment::value).map((lines) -> String.join("\n", lines)).orElse("");
            val name = Optional.of(field.getAnnotation(Config.Name.class)).map(Config.Name::value).orElse(field.getName());
            val langKey = Optional.of(field.getAnnotation(Config.LangKey.class)).map(Config.LangKey::value).orElse(name);
            var boxed = false;
            Property prop = cat.get(name);
            prop.comment = comment;
            prop.setLanguageKey(langKey);
            if ((boxed = field.getType().equals(Integer.class)) || field.getType().equals(int.class)) {
                val range = Optional.of(field.getAnnotation(Config.RangeInt.class));
                prop.setMinValue(range.map(Config.RangeInt::min).orElse(Integer.MIN_VALUE));
                prop.setMaxValue(range.map(Config.RangeInt::max).orElse(Integer.MAX_VALUE));
                prop.setDefaultValue(Optional.of(field.getAnnotation(Config.DefaultInt.class)).map(Config.DefaultInt::value).orElse(boxed ? (Integer)field.get(null) : field.getInt(null)));
                field.setInt(null, prop.getInt());
            } else if ((boxed = field.getType().equals(Double.class)) || field.getType().equals(double.class)) {
                val range = Optional.of(field.getAnnotation(Config.RangeDouble.class));
                prop.setMinValue(range.map(Config.RangeDouble::min).orElse(Double.MIN_VALUE));
                prop.setMaxValue(range.map(Config.RangeDouble::max).orElse(Double.MAX_VALUE));
                prop.setDefaultValue(Optional.of(field.getAnnotation(Config.DefaultDouble.class)).map(Config.DefaultDouble::value).orElse(boxed ? (Double) field.get(null) : field.getDouble(null)));
                field.setDouble(null, prop.getDouble());
            } else if ((boxed = field.getType().equals(Boolean.class)) || field.getType().equals(boolean.class)) {
                prop.setDefaultValue(boxed ? (Boolean)field.get(null) : field.getBoolean(null));
                field.setBoolean(null, prop.getBoolean());
            } else if (field.getType().equals(String.class)) {
                prop.setDefaultValue((String)field.get(null));
                field.set(null, prop.getString());
            }
            if (field.isAnnotationPresent(Config.RequiresMcRestart.class)) {
                cat.setRequiresMcRestart(true);
            }
            if (field.isAnnotationPresent(Config.RequiresWorldRestart.class)) {
                cat.setRequiresWorldRestart(true);
            }
        }
    }
}
