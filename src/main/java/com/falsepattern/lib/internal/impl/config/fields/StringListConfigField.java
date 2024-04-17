/*
 * Copyright (C) 2022-2023 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
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
package com.falsepattern.lib.internal.impl.config.fields;

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigException;
import lombok.val;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public class StringListConfigField extends AListConfigField<String[]> {
    private final String[] defaultValue;
    private final Pattern pattern;
    private final int maxStringLength;

    public StringListConfigField(Field field, Configuration configuration, String category) throws ConfigException {
        super(field, configuration, category, Property.Type.STRING);
        pattern = Optional.ofNullable(field.getAnnotation(Config.Pattern.class))
                          .map((ptr) -> Pattern.compile(ptr.value()))
                          .orElse(null);
        defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultStringList.class))
                               .map(Config.DefaultStringList::value)
                               .orElseThrow(() -> noDefault(field, "DefaultStringList"));
        maxStringLength = Optional.ofNullable(field.getAnnotation(Config.StringMaxLength.class))
                                  .map(Config.StringMaxLength::value)
                                  .orElse(256);
        property.setDefaultValues(defaultValue);
        if (!property.isList()) {
            setToDefault();
        }
        property.comment +=
                "\n[max string length: "
                + maxLength
                + (pattern != null ? ", pattern: \"" + pattern.pattern() + "\"" : "")
                + ", default: \""
                + stringify(defaultValue)
                + "\"]";
    }

    private static String stringify(String[] arr) {
        val result = new StringBuilder("[");
        if (arr.length > 0) {
            result.append('"').append(arr[0]).append('"');
            for (int i = 1; i < arr.length; i++) {
                result.append(", \"").append(arr[i]).append('"');
            }
        }
        return result.append(']').toString();
    }

    @Override
    protected int length(String[] arr) {
        return arr.length;
    }

    @Override
    protected String[] arrayCopy(String[] arr) {
        return Arrays.copyOf(arr, arr.length);
    }

    @Override
    protected void transmitElements(DataOutput output, String[] arr) throws IOException {
        for (val str : arr) {
            StringConfigField.transmitString(output, str);
        }
    }

    @Override
    protected void receiveElements(DataInput input, String[] arr) throws IOException {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = StringConfigField.receiveString(input,
                                                     maxStringLength,
                                                     field.getName(),
                                                     field.getDeclaringClass().getName());
        }
    }

    @Override
    public boolean validateField() {
        if (!super.validateField()) {
            return false;
        }
        String[] strings = getField();
        boolean valid = true;
        for (int i = 0; i < strings.length; i++) {
            String str = strings[i];
            if (!StringConfigField.validateString(str, maxStringLength, pattern, field, i)) {
                valid = false;
            }
        }
        return valid;
    }

    @Override
    protected String[] createArray(int length) {
        return new String[length];
    }

    @Override
    protected String[] getConfig() {
        return property.getStringList();
    }

    @Override
    protected void putConfig(String[] value) {
        property.set(value);
    }

    @Override
    protected String[] getDefault() {
        return Arrays.copyOf(defaultValue, defaultValue.length);
    }
}
