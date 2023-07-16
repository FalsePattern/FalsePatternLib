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

public class DoubleConfigField extends AConfigField<Double> {
    private final boolean primitive;
    private final double defaultValue;
    private final double min;
    private final double max;

    public DoubleConfigField(Field field, Configuration configuration, String category) {
        super(field, configuration, category, Property.Type.DOUBLE);
        primitive = field.getType().isPrimitive();
        val range = Optional.ofNullable(field.getAnnotation(Config.RangeDouble.class));
        min = range.map(Config.RangeDouble::min).orElse(-Double.MAX_VALUE);
        max = range.map(Config.RangeDouble::max).orElse(Double.MAX_VALUE);
        defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultDouble.class))
                               .map(Config.DefaultDouble::value)
                               .orElseGet(() -> {
                                   Share.LOG
                                                  .warn("The field " + field.getName() + " in class " +
                                                        field.getDeclaringClass().getName() +
                                                        " has no DefaultDouble annotation!\nThis will be a crash in FalsePatternLib 0.11, update your code!");
                                   try {
                                       return field.getType().isPrimitive() ? field.getInt(null)
                                                                            : (Double) field.get(null);
                                   } catch (IllegalAccessException e) {
                                       throw new RuntimeException(e);
                                   }
                               });
        property.setDefaultValue(defaultValue);
        property.setMinValue(min);
        property.setMaxValue(max);
        if (!property.isDoubleValue()) {
            setToDefault();
        }
        property.comment += "\n[range: " + min + " ~ " + max + ", default: " + defaultValue + "]";
    }

    @SneakyThrows
    @Override
    protected Double getField() {
        return primitive ? field.getDouble(null) : (Double) field.get(null);
    }

    @SneakyThrows
    @Override
    protected void putField(Double value) {
        if (primitive) {
            field.setDouble(null, value);
        } else {
            field.set(null, value);
        }
    }

    @Override
    protected Double getConfig() {
        return property.getDouble();
    }

    @Override
    protected void putConfig(Double value) {
        property.set(value);
    }

    @Override
    protected Double getDefault() {
        return defaultValue;
    }

    @SneakyThrows
    @Override
    public boolean validateField() {
        if (!primitive && field.get(null) == null) {
            ConfigValidationFailureEvent.fieldIsNull(field, -1);
            return false;
        }
        val value = (double) getField();
        if (value >= min && value <= max) {
            return true;
        }
        ConfigValidationFailureEvent.postNumericRangeOutOfBounds(field, -1, Double.toString(value),
                                                                 Double.toString(min), Double.toString(max));
        return false;
    }

    @Override
    public void transmit(DataOutput output) throws IOException {
        output.writeDouble(getField());
    }

    @Override
    public void receive(DataInput input) throws IOException {
        putField(input.readDouble());
    }
}
