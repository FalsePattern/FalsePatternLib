/*
 * This file is part of FalsePatternLib.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalsePatternLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
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
import com.falsepattern.lib.internal.impl.config.ConfigFieldParameters;
import lombok.SneakyThrows;
import lombok.val;

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

    public StringConfigField(ConfigFieldParameters params) {
        super(params, Property.Type.STRING);
        pattern = Optional.ofNullable(field.getAnnotation(Config.Pattern.class))
                          .map((ptr) -> Pattern.compile(ptr.value()))
                          .orElse(null);
        defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultString.class))
                               .map(Config.DefaultString::value)
                               .orElseThrow(() -> noDefault(field, "DefaultString"));
        maxLength = Optional.ofNullable(field.getAnnotation(Config.StringMaxLength.class))
                            .map(Config.StringMaxLength::value)
                            .orElse(-1);
        val property = getProperty();
        property.setDefaultValue(defaultValue);
        property.comment += generateStringComment(maxLength, pattern, defaultValue);
    }

    public static String generateStringComment(int maxStringLength, Pattern pattern, String defaultValue) {
        val cmt = new StringBuilder();
        boolean comma = false;
        if (maxStringLength >= 0) {
            cmt.append("\n[max string length: ").append(maxStringLength);
            comma = true;
        }
        if (pattern != null) {
            if (comma) {
                cmt.append(", ");
            } else {
                cmt.append("\n[");
            }
            cmt.append("pattern: \"").append(pattern.pattern()).append("\"");
            comma = true;
        }
        if (comma) {
            cmt.append(", ");
        } else {
            cmt.append("\n[");
        }
        cmt.append("default: ").append(defaultValue);
        cmt.append("]");
        return cmt.toString();
    }

    public static boolean validateString(String value, int maxLength, Pattern pattern, Field field, int listIndex) {
        if (value == null) {
            ConfigValidationFailureEvent.fieldIsNull(field, listIndex);
            return false;
        }
        if (maxLength >= 0 && value.length() > maxLength) {
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
        if ((maxLength >= 0 && length > maxLength) || length < 0) {
            throw new IOException("Error while retrieving value for "
                                  + valueName
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
        return getProperty().getString();
    }

    @Override
    protected void putConfig(String value) {
        getProperty().set(value);
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
