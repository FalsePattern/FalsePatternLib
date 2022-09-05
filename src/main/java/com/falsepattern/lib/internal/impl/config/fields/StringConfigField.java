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
package com.falsepattern.lib.internal.impl.config.fields;

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.event.ConfigValidationFailureEvent;
import com.falsepattern.lib.internal.Share;
import lombok.SneakyThrows;
import lombok.val;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.regex.Pattern;

public class StringConfigField extends AConfigField<String> {
    private final int maxLength;
    private final Pattern pattern;
    private final String defaultValue;

    public StringConfigField(Field field, Configuration configuration, String category) {
        super(field, configuration, category, Property.Type.STRING);
        pattern = Optional.ofNullable(field.getAnnotation(Config.Pattern.class))
                          .map((ptr) -> Pattern.compile(ptr.value()))
                          .orElse(null);
        defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultString.class))
                               .map(Config.DefaultString::value)
                               .orElseGet(() -> {
                                   Share.LOG
                                                  .warn("The field " + field.getName() + " in class " +
                                                        field.getDeclaringClass().getName() +
                                                        " has no DefaultString annotation!\nThis will be a crash in FalsePatternLib 0.11, update your code!");
                                   try {
                                       return (String) field.get(null);
                                   } catch (IllegalAccessException e) {
                                       throw new RuntimeException(e);
                                   }
                               });
        maxLength = Optional.ofNullable(field.getAnnotation(Config.StringMaxLength.class))
                            .map(Config.StringMaxLength::value)
                            .orElse(256);
        property.setDefaultValue(defaultValue);
        property.comment +=
                "\n[max length: " + maxLength + (pattern != null ? ", pattern: \"" + pattern.pattern() + "\"" : "") +
                ", default: \"" + defaultValue + "\"]";
    }

    public static boolean validateString(String value, int maxLength, Pattern pattern, Field field, int listIndex) {
        if (value == null) {
            ConfigValidationFailureEvent.fieldIsNull(field, listIndex);
            return false;
        }
        if (value.length() > maxLength) {
            ConfigValidationFailureEvent.postStringSizeOutOfBounds(field, listIndex, value, maxLength);
            return false;
        }
        if (pattern != null && !pattern.matcher(value).matches()) {
            ConfigValidationFailureEvent.postStringPatternMismatch(field, listIndex, value, pattern.pattern());
            return false;
        }
        return true;
    }

    public static void transmitString(DataOutput output, String value) throws IOException {
        output.writeInt(value.length());
        output.writeChars(value);
    }

    public static String receiveString(DataInput input, int maxLength, String valueName, String className)
            throws IOException {
        val length = input.readInt();
        if (length > maxLength || length < 0) {
            throw new IOException("Error while retrieving value for " + valueName + " in class " + className + ":\n" +
                                  "Illegal string length received!");
        }
        val arr = new char[length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = input.readChar();
        }
        return new String(arr);
    }

    @SneakyThrows
    @Override
    protected String getField() {
        return (String) field.get(null);
    }

    @SneakyThrows
    @Override
    protected void putField(String value) {
        field.set(null, value);
    }

    @Override
    protected String getConfig() {
        return property.getString();
    }

    @Override
    protected void putConfig(String value) {
        property.set(value);
    }

    @Override
    protected String getDefault() {
        return defaultValue;
    }

    @SneakyThrows
    @Override
    public boolean validateField() {
        return validateString(getField(), maxLength, pattern, field, -1);
    }

    @Override
    public void transmit(DataOutput output) throws IOException {
        transmitString(output, getField());
    }

    @Override
    public void receive(DataInput input) throws IOException {
        putField(receiveString(input, maxLength, field.getName(), field.getDeclaringClass().getName()));
    }
}
