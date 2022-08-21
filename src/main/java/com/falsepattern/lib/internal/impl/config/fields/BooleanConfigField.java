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
import com.falsepattern.lib.internal.FalsePatternLib;
import lombok.SneakyThrows;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;

public class BooleanConfigField extends AConfigField<Boolean> {
    private final boolean primitive;
    private final boolean defaultValue;

    public BooleanConfigField(Field field, Configuration configuration, String category) {
        super(field, configuration, category, Property.Type.BOOLEAN);
        primitive = field.getType().isPrimitive();
        defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultBoolean.class))
                               .map(Config.DefaultBoolean::value)
                               .orElseGet(() -> {
                                   FalsePatternLib.getLog()
                                                  .warn("The field " + field.getName() + " in class " +
                                                        field.getDeclaringClass().getName() +
                                                        " has no DefaultBoolean annotation!\nThis will be a crash in FalsePatternLib 0.11, update your code!");
                                   try {
                                       return field.getType().isPrimitive() ? field.getBoolean(null)
                                                                            : (Boolean) field.get(null);
                                   } catch (IllegalAccessException e) {
                                       throw new RuntimeException(e);
                                   }
                               });
        property.setDefaultValue(defaultValue);
        if (!property.isBooleanValue()) {
            setToDefault();
        }
        property.comment += "\n[default: " + defaultValue + "]";
    }

    @SneakyThrows
    @Override
    protected Boolean getField() {
        return primitive ? field.getBoolean(null) : (Boolean) field.get(null);
    }

    @SneakyThrows
    @Override
    protected void putField(Boolean value) {
        if (primitive) {
            field.setBoolean(null, value);
        } else {
            field.set(null, value);
        }
    }

    @Override
    protected Boolean getConfig() {
        return property.getBoolean();
    }

    @Override
    protected void putConfig(Boolean value) {
        property.set(value);
    }

    @Override
    protected Boolean getDefault() {
        return defaultValue;
    }

    @SneakyThrows
    @Override
    public boolean validateField() {
        if (!primitive && field.get(null) == null) {
            ConfigValidationFailureEvent.fieldIsNull(field, -1);
            return false;
        }
        return true;
    }

    @Override
    public void transmit(DataOutput output) throws IOException {
        output.writeBoolean(getField());
    }

    @Override
    public void receive(DataInput input) throws IOException {
        putField(input.readBoolean());
    }
}
