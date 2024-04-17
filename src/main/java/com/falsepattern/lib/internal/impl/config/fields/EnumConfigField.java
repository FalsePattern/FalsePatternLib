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
package com.falsepattern.lib.internal.impl.config.fields;

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.event.ConfigValidationFailureEvent;
import com.falsepattern.lib.internal.FPLog;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.SneakyThrows;
import lombok.val;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public class EnumConfigField<T extends Enum<T>> extends AConfigField<T> {
    private final int maxLength;
    private final Class<T> enumClass;
    private final T defaultValue;
    private final BiMap<String, T> enumNameMap = HashBiMap.create();

    @SuppressWarnings("unchecked")
    public EnumConfigField(Field field, Configuration configuration, String category) {
        super(field, configuration, category, Property.Type.STRING);
        enumClass = (Class<T>) field.getType();
        for (val e : enumClass.getEnumConstants()) {
            enumNameMap.put(e.name(), e);
        }
        defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultEnum.class))
                               .map(Config.DefaultEnum::value)
                               .map((defName) -> enumNameMap.values()
                                                            .stream()
                                                            .filter((e) -> e.name().equals(defName))
                                                            .findAny()
                                                            .orElseThrow(() -> new IllegalArgumentException(
                                                                    "Default value \""
                                                                    + defName
                                                                    + "\" was not found in enum "
                                                                    + enumClass.getName())))
                               .orElseThrow(() -> noDefault(field, "DefaultEnum"));
        maxLength = enumNameMap.keySet().stream().mapToInt(String::length).max().orElse(0);
        property.setDefaultValue(defaultValue.name());
        property.setValidValues(enumNameMap.keySet().toArray(new String[0]));
        if (!enumNameMap.containsValue(getConfig())) {
            setToDefault();
        }
        property.comment += "\n[default: "
                            + defaultValue
                            + ", possible values: "
                            + Arrays.toString(enumNameMap.keySet()
                                                         .toArray(new String[0]))
                            + "]";
    }

    public static void transmitString(DataOutput output, String value) throws IOException {
        output.writeInt(value.length());
        output.writeChars(value);
    }

    public static String receiveString(DataInput input, int maxLength, String fieldName, String className)
            throws IOException {
        val length = input.readInt();
        if (length > maxLength || length < 0) {
            throw new IOException("Error while retrieving config value for field "
                                  + fieldName
                                  + " in class "
                                  + className
                                  + ":\n"
                                  + "Illegal string length received!");
        }
        val arr = new char[length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = input.readChar();
        }
        return new String(arr);
    }

    private T getEnumByName(String name) {
        if (!enumNameMap.containsKey(name)) {
            FPLog.LOG.warn("Invalid value "
                           + name
                           + " for enum configuration field "
                           + field.getName()
                           + " of type "
                           + enumClass.getName()
                           + " in config class "
                           + field.getDeclaringClass().getName()
                           + "! Using default value of "
                           + defaultValue.name()
                           + "!");
            return defaultValue;
        }
        return enumNameMap.get(name);
    }

    @SneakyThrows
    @Override
    protected T getField() {
        return enumClass.cast(field.get(null));
    }

    @SneakyThrows
    @Override
    protected void putField(T value) {
        field.set(null, value);
    }

    @Override
    protected T getConfig() {
        return getEnumByName(property.getString());
    }

    @Override
    protected void putConfig(T value) {
        property.set(value.name());
    }

    @Override
    protected T getDefault() {
        return defaultValue;
    }

    @SneakyThrows
    @Override
    public boolean validateField() {
        if (getField() == null) {
            ConfigValidationFailureEvent.fieldIsNull(field, -1);
            return false;
        }
        return true;
    }

    @Override
    public void transmit(DataOutput output) throws IOException {
        transmitString(output, getField().name());
    }

    @Override
    public void receive(DataInput input) throws IOException {
        putField(getEnumByName(receiveString(input, maxLength, field.getName(), field.getDeclaringClass().getName())));
    }
}
