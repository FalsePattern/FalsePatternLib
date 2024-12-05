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

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.internal.impl.config.fields.AConfigField;
import com.falsepattern.lib.internal.impl.config.fields.BooleanConfigField;
import com.falsepattern.lib.internal.impl.config.fields.BooleanListConfigField;
import com.falsepattern.lib.internal.impl.config.fields.DoubleConfigField;
import com.falsepattern.lib.internal.impl.config.fields.DoubleListConfigField;
import com.falsepattern.lib.internal.impl.config.fields.EnumConfigField;
import com.falsepattern.lib.internal.impl.config.fields.IntConfigField;
import com.falsepattern.lib.internal.impl.config.fields.IntListConfigField;
import com.falsepattern.lib.internal.impl.config.fields.StringConfigField;
import com.falsepattern.lib.internal.impl.config.fields.StringListConfigField;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.client.config.IConfigElement;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ParsedConfiguration {
    private static final Map<Class<?>, FieldRefConstructor> constructors = new HashMap<>();

    static {
        constructors.put(Boolean.class, BooleanConfigField::new);
        constructors.put(boolean.class, BooleanConfigField::new);
        constructors.put(Integer.class, IntConfigField::new);
        constructors.put(int.class, IntConfigField::new);
        constructors.put(Double.class, DoubleConfigField::new);
        constructors.put(double.class, DoubleConfigField::new);
        constructors.put(String.class, StringConfigField::new);
        constructors.put(boolean[].class, BooleanListConfigField::new);
        constructors.put(int[].class, IntListConfigField::new);
        constructors.put(double[].class, DoubleListConfigField::new);
        constructors.put(String[].class, StringListConfigField::new);
    }

    public final Class<?> configClass;
    public final String modid;
    public final String category;
    public final String comment;
    public final String langKey;
    public final Configuration rawConfig;
    public final boolean sync;
    private final Map<String, AConfigField<?>> fields = new HashMap<>();
    private final Map<String, IConfigElement<?>> elements = new HashMap<>();
    private int maxFieldNameLength;

    public static ParsedConfiguration parseConfig(Class<?> configClass) throws ConfigException {
        val cfg = Optional.ofNullable(configClass.getAnnotation(Config.class))
                          .orElseThrow(() -> new ConfigException("Class "
                                                                 + configClass.getName()
                                                                 + " does not have a @Config annotation!"));
        val category = Optional.of(cfg.category().trim())
                               .map((cat) -> cat.isEmpty() ? null : cat)
                               .orElseThrow(() -> new ConfigException("Config class "
                                                                      + configClass.getName()
                                                                      + " has an empty category!"));
        val comment = Optional.ofNullable(configClass.getAnnotation(Config.Comment.class))
                          .map(Config.Comment::value)
                          .map((lines) -> String.join("\n", lines))
                          .orElse("");
        val langKey = Optional.ofNullable(configClass.getAnnotation(Config.LangKey.class))
                              .map(Config.LangKey::value)
                              .map(x -> x.isEmpty() ? "config." + cfg.modid() + "." + category : x)
                              .orElse(category);
        val path = Optional.of(cfg.customPath().trim())
                           .map(p -> p.isEmpty() ? null : p)
                           .orElse(cfg.modid());
        val rawConfig = ConfigurationManagerImpl.getForgeConfig(path, true);
        if (!rawConfig.hasCategory(category)) {
            // Process migrations
            migrate:
            {
                val categoryCandidates = new ArrayList<>(Arrays.asList(cfg.categoryMigrations()));
                categoryCandidates.add(0, category);
                for (var migration : cfg.pathMigrations()) {
                    migration = migration.trim();
                    try {
                        val oldConfig = ConfigurationManagerImpl.getForgeConfig(migration, false);
                        for (val fromCategory : categoryCandidates) {
                            if (oldConfig.hasCategory(fromCategory)) {
                                val oldCat = oldConfig.getCategory(fromCategory);
                                val newCat = rawConfig.getCategory(category);
                                val entries = oldCat.keySet();
                                for (val entry : entries) {
                                    newCat.put(entry, oldCat.get(entry));
                                }
                                rawConfig.save();
                                oldConfig.removeCategory(oldConfig.getCategory(category));
                                if (oldConfig.getCategoryNames().isEmpty()) {
                                    oldConfig.getConfigFile().delete();
                                } else {
                                    oldConfig.save();
                                }
                                break migrate;
                            }
                        }
                    } catch (ConfigException ignored) {
                    }
                }
                // Try to migrate category in local file
                categoryCandidates.remove(0);
                for (val fromCategory : categoryCandidates) {
                    if (rawConfig.hasCategory(fromCategory)) {
                        val oldCategory = rawConfig.getCategory(fromCategory);
                        val newCategory = rawConfig.getCategory(category);
                        val entries = oldCategory.keySet();
                        for (val entry: entries) {
                            newCategory.put(entry, oldCategory.get(entry));
                        }
                        rawConfig.removeCategory(oldCategory);
                        break migrate;
                    }
                }
            }
        }
        rawConfig.setCategoryComment(category, comment);
        rawConfig.setCategoryLanguageKey(category, langKey);
        val parsedConfig = new ParsedConfiguration(configClass,
                                                   cfg.modid(),
                                                   category,
                                                   comment,
                                                   langKey,
                                                   rawConfig,
                                                   configClass.isAnnotationPresent(Config.Synchronize.class));
        try {
            parsedConfig.reloadFields();
        } catch (IllegalAccessException e) {
            throw new ConfigException(e);
        }
        return parsedConfig;
    }

    public void saveFile() {
        saveFields();
        rawConfig.save();
    }

    public void saveFields() {
        for (val field : fields.values()) {
            field.save();
        }
    }

    //Happens when changed through the gui
    public void configChanged() {
        for (val field : fields.values()) {
            field.load();
        }
        rawConfig.save();
    }

    public void loadFile() throws ConfigException {
        ConfigurationManagerImpl.loadRawConfig(rawConfig);
        loadFields();
    }

    public void loadFields() {
        for (val field : fields.values()) {
            field.load();
        }
    }

    public void receive(DataInput input) throws IOException {
        if (sync) {
            val syncFields = new HashMap<>(fields);
            for (val key : fields.keySet()) {
                if (syncFields.get(key).noSync) {
                    syncFields.remove(key);
                }
            }
            while (!syncFields.isEmpty()) {
                val fieldName =
                        StringConfigField.receiveString(input, maxFieldNameLength, "field name", configClass.getName());
                if (!syncFields.containsKey(fieldName)) {
                    throw new IOException("Invalid sync field name received: "
                                          + fieldName
                                          + " for config class "
                                          + configClass.getName());
                }
                syncFields.remove(fieldName).receive(input);
            }
        }
    }

    public void transmit(DataOutput output) throws IOException {
        if (sync) {
            val syncFields = new HashMap<>(fields);
            for (val key : fields.keySet()) {
                if (syncFields.get(key).noSync) {
                    syncFields.remove(key);
                }
            }
            for (val field : syncFields.entrySet()) {
                StringConfigField.transmitString(output, field.getKey());
                field.getValue().transmit(output);
            }
        }
    }

    public void reloadFields() throws ConfigException, IllegalAccessException {
        fields.clear();
        maxFieldNameLength = 0;
        val cat = rawConfig.getCategory(category);
        if (configClass.isAnnotationPresent(Config.RequiresWorldRestart.class)) {
            cat.setRequiresWorldRestart(true);
        }
        if (configClass.isAssignableFrom(Config.RequiresMcRestart.class)) {
            cat.setRequiresMcRestart(true);
        }
        val nonFoundKeys = new HashSet<>(rawConfig.getCategory(category).keySet());
        for (val field : configClass.getDeclaredFields()) {
            if (field.getAnnotation(Config.Ignore.class) != null || (field.getModifiers() & Modifier.FINAL) != 0) {
                continue;
            }
            field.setAccessible(true);
            maxFieldNameLength = Math.max(maxFieldNameLength, field.getName().length());
            val fieldClass = field.getType();
            val nameAnnotation = Optional.ofNullable(field.getAnnotation(Config.Name.class));
            val name = nameAnnotation.map(Config.Name::value)
                                     .orElse(field.getName());
            if (!cat.containsKey(name) && nameAnnotation.isPresent()) {
                val migrations = nameAnnotation.get().migrations();
                for (var migration: migrations) {
                    if (migration.isEmpty()) {
                        migration = field.getName();
                    }
                    if (cat.containsKey(migration)) {
                        val prop = cat.remove(migration);
                        prop.setName(name);
                        cat.put(name, prop);
                        nonFoundKeys.remove(migration);
                        break;
                    }
                }
            } else {
                nonFoundKeys.remove(name);
            }
            AConfigField<?> configField;
            val params = new ConfigFieldParameters(field, rawConfig, modid, category);
            if (constructors.containsKey(fieldClass)) {
                fields.put(name, configField = constructors.get(fieldClass).construct(params));
            } else if (fieldClass.isEnum()) {
                fields.put(name, configField = new EnumConfigField<>(params));
            } else {
                throw new ConfigException("Illegal config field: "
                                          + field.getName()
                                          + " in "
                                          + configClass.getName()
                                          + ": Unsupported type "
                                          + fieldClass.getName()
                                          + "! Did you forget an @Ignore annotation?");
            }
            configField.init();
            elements.computeIfAbsent(name, (name2) -> new ConfigElementProxy<>(configField.getProperty(), configField.getComment(), () -> {
                configField.load();
                configField.save();
            }));
        }
        val rawCategory = rawConfig.getCategory(category);
        for (val key : nonFoundKeys) {
            rawCategory.remove(key);
        }
        saveFile();
        rawConfig.setCategoryPropertyOrder(category,
                                           fieldsSorted().map((prop) -> prop.name).collect(Collectors.toList()));
    }

    private Stream<AConfigField<?>> fieldsSorted() {
        return fields.values().stream().sorted(Comparator.comparingInt((prop) -> prop.order));
    }

    @SuppressWarnings("rawtypes")
    public List<IConfigElement> getConfigElements() {
        return fieldsSorted().map((field) -> elements.get(field.name)).collect(Collectors.toList());
    }

    public boolean requiresWorldRestart() {
        return rawConfig.getCategory(category).requiresWorldRestart();
    }

    public boolean requiresMcRestart() {
        return rawConfig.getCategory(category).requiresMcRestart();
    }

    public boolean validate(BiConsumer<Class<?>, Field> invalidFieldHandler, boolean resetInvalid) {
        boolean valid = true;
        for (val field : fields.values()) {
            if (!field.validateField()) {
                if (resetInvalid) {
                    field.setToDefault();
                }
                invalidFieldHandler.accept(configClass, field.getJavaField());
                valid = false;
            }
        }
        return valid;
    }

    private interface FieldRefConstructor {
        AConfigField<?> construct(ConfigFieldParameters params) throws ConfigException;
    }
}
