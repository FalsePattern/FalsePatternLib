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
package com.falsepattern.lib.internal.impl.config;

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.internal.impl.config.fields.AConfigField;
import com.falsepattern.lib.internal.impl.config.fields.BooleanConfigField;
import com.falsepattern.lib.internal.impl.config.fields.BooleanListConfigField;
import com.falsepattern.lib.internal.impl.config.fields.DoubleConfigField;
import com.falsepattern.lib.internal.impl.config.fields.DoubleListConfigField;
import com.falsepattern.lib.internal.impl.config.fields.EnumConfigField;
import com.falsepattern.lib.internal.impl.config.fields.FloatConfigField;
import com.falsepattern.lib.internal.impl.config.fields.IntConfigField;
import com.falsepattern.lib.internal.impl.config.fields.IntListConfigField;
import com.falsepattern.lib.internal.impl.config.fields.StringConfigField;
import com.falsepattern.lib.internal.impl.config.fields.StringListConfigField;
import com.falsepattern.lib.internal.ReflectionUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.client.config.IConfigElement;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ParsedConfiguration {
    private static final Field propField;

    static {
        try {
            propField = ConfigElement.class.getDeclaredField("prop");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        ReflectionUtil.jailBreak(propField);
    }

    public final Class<?> configClass;
    public final String modid;
    public final String category;
    public final Configuration rawConfig;
    public final boolean sync;
    private final Map<String, AConfigField<?>> fields = new HashMap<>();
    private final Map<String, ConfigElement<?>> elements = new HashMap<>();
    private int maxFieldNameLength;

    public static ParsedConfiguration parseConfig(Class<?> configClass) throws ConfigException {
        val cfg = Optional.ofNullable(configClass.getAnnotation(Config.class))
                          .orElseThrow(() -> new ConfigException(
                                  "Class " + configClass.getName() + " does not have a @Config annotation!"));
        val category = Optional.of(cfg.category().trim())
                               .map((cat) -> cat.length() == 0 ? null : cat)
                               .orElseThrow(() -> new ConfigException(
                                       "Config class " + configClass.getName() + " has an empty category!"));
        val rawConfig = ConfigurationManagerImpl.getForgeConfig(cfg.modid());
        val parsedConfig = new ParsedConfiguration(configClass, cfg.modid(), category, rawConfig,
                                                   configClass.isAnnotationPresent(Config.Synchronize.class));
        try {
            parsedConfig.reloadFields();
        } catch (IllegalAccessException e) {
            throw new ConfigException(e);
        }
        return parsedConfig;
    }

    public void save() {
        for (val field : fields.values()) {
            field.save();
        }
        rawConfig.save();
    }

    //Happens when changed through the gui
    public void configChanged() {
        for (val field : fields.values()) {
            field.load();
        }
        rawConfig.save();
    }

    public void load() throws ConfigException {
        ConfigurationManagerImpl.loadRawConfig(rawConfig);
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
            while (syncFields.size() > 0) {
                val fieldName =
                        StringConfigField.receiveString(input, maxFieldNameLength, "field name", configClass.getName());
                if (!syncFields.containsKey(fieldName)) {
                    throw new IOException("Invalid sync field name received: " + fieldName + " for config class " +
                                          configClass.getName());
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
        for (val field : configClass.getDeclaredFields()) {
            if (field.getAnnotation(Config.Ignore.class) != null && (field.getModifiers() & Modifier.FINAL) != 0) {
                continue;
            }
            field.setAccessible(true);
            maxFieldNameLength = Math.max(maxFieldNameLength, field.getName().length());
            val fieldClass = field.getType();
            val name = field.getName();
            if (fieldClass.equals(Boolean.class) || fieldClass.equals(boolean.class)) {
                fields.put(name, new BooleanConfigField(field, rawConfig, category));
            } else if (fieldClass.equals(Integer.class) || fieldClass.equals(int.class)) {
                fields.put(name, new IntConfigField(field, rawConfig, category));
            } else if (fieldClass.equals(Float.class) || fieldClass.equals(float.class)) {
                //noinspection deprecation
                fields.put(name, new FloatConfigField(field, rawConfig, category));
            } else if (fieldClass.equals(Double.class) || fieldClass.equals(double.class)) {
                fields.put(name, new DoubleConfigField(field, rawConfig, category));
            } else if (fieldClass.equals(String.class)) {
                fields.put(name, new StringConfigField(field, rawConfig, category));
            } else if (fieldClass.isEnum()) {
                fields.put(name, new EnumConfigField<>(field, rawConfig, category));
            } else if (fieldClass.isArray() && fieldClass.getComponentType().equals(boolean.class)) {
                fields.put(name, new BooleanListConfigField(field, rawConfig, category));
            } else if (fieldClass.isArray() && fieldClass.getComponentType().equals(int.class)) {
                fields.put(name, new IntListConfigField(field, rawConfig, category));
            } else if (fieldClass.isArray() && fieldClass.getComponentType().equals(double.class)) {
                fields.put(name, new DoubleListConfigField(field, rawConfig, category));
            } else if (fieldClass.isArray() && fieldClass.getComponentType().equals(String.class)) {
                fields.put(name, new StringListConfigField(field, rawConfig, category));
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
            val prop = fields.get(name).getProperty();
            val configElement = elements.computeIfAbsent(name, (name2) -> new ConfigElement<>(prop));
            propField.set(configElement, prop);
        }
    }

    @SuppressWarnings("rawtypes")
    public List<IConfigElement> getConfigElements() {
        return new ArrayList<>(elements.values());
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
}
