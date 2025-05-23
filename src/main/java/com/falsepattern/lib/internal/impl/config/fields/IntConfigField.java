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
import java.util.Optional;

public class IntConfigField extends AConfigField<Integer> {
    private final boolean primitive;
    private final int defaultValue;
    private final int min;
    private final int max;

    public IntConfigField(ConfigFieldParameters params) {
        super(params, Property.Type.INTEGER);
        primitive = field.getType().isPrimitive();
        val range = Optional.ofNullable(field.getAnnotation(Config.RangeInt.class));
        min = range.map(Config.RangeInt::min).orElse(Integer.MIN_VALUE);
        max = range.map(Config.RangeInt::max).orElse(Integer.MAX_VALUE);
        defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultInt.class))
                               .map(Config.DefaultInt::value)
                               .orElseThrow(() -> noDefault(field, "DefaultInt"));
        val property = getProperty();
        property.setDefaultValue(defaultValue);
        property.setMinValue(min);
        property.setMaxValue(max);
        if (!property.isIntValue()) {
            setToDefault();
        }
        property.comment += "\n[range: " + min + " ~ " + max + ", default: " + defaultValue + "]";
    }

    @SneakyThrows
    @Override
    protected Integer getField() {
        return primitive ? field.getInt(null) : (Integer) field.get(null);
    }

    @SneakyThrows
    @Override
    protected void putField(Integer value) {
        if (primitive) {
            field.setInt(null, value);
        } else {
            field.set(null, value);
        }
    }

    @Override
    protected Integer getConfig() {
        return getProperty().getInt();
    }

    @Override
    protected void putConfig(Integer value) {
        getProperty().set(value);
    }

    @Override
    protected Integer getDefault() {
        return defaultValue;
    }

    @SneakyThrows
    @Override
    public boolean validateField() {
        if (!primitive && field.get(null) == null) {
            ConfigValidationFailureEvent.fieldIsNull(field, -1);
            return false;
        }
        val value = (int) getField();
        if (value >= min && value <= max) {
            return true;
        }
        ConfigValidationFailureEvent.postNumericRangeOutOfBounds(field,
                                                                 -1,
                                                                 Integer.toString(value),
                                                                 Integer.toString(min),
                                                                 Integer.toString(max));
        return false;
    }

    @Override
    public void transmit(DataOutput output) throws IOException {
        output.writeInt(getField());
    }

    @Override
    public void receive(DataInput input) throws IOException {
        putField(input.readInt());
    }
}
