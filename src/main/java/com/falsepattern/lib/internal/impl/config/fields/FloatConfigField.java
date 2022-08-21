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
import com.falsepattern.lib.util.MathUtil;
import lombok.SneakyThrows;
import lombok.val;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;

@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class FloatConfigField extends AConfigField<Float> {
    private final boolean primitive;
    private final float defaultValue;
    private final boolean hasMinMax;
    private final float min;
    private final float max;

    public FloatConfigField(Field field, Configuration configuration, String category) {
        super(field, configuration, category, Property.Type.STRING);
        FalsePatternLib.getLog()
                       .warn("Warning: float configuration properties are DEPRECATED! Use doubles instead!\n" +
                             "Field name: " + field.getName() + ", location: " + field.getDeclaringClass().getName());
        primitive = field.getType().isPrimitive();
        val range = Optional.ofNullable(field.getAnnotation(Config.RangeFloat.class));
        hasMinMax = range.isPresent();
        min = hasMinMax ? range.map(Config.RangeFloat::min).orElse(-Float.MAX_VALUE) : 0;
        max = hasMinMax ? range.map(Config.RangeFloat::max).orElse(Float.MAX_VALUE) : 0;
        defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultFloat.class))
                               .map(Config.DefaultFloat::value)
                               .orElseGet(() -> {
                                   try {
                                       return field.getType().isPrimitive() ? field.getFloat(null)
                                                                            : (Float) field.get(null);
                                   } catch (IllegalAccessException e) {
                                       throw new RuntimeException(e);
                                   }
                               });
        property.setDefaultValue(defaultValue);
        property.setMinValue(min);
        property.setMaxValue(max);
        property.comment += "\n[range: " + min + " ~ " + max + ", default: " + defaultValue + "]";
    }

    @SneakyThrows
    @Override
    protected Float getField() {
        return primitive ? field.getFloat(null) : (Integer) field.get(null);
    }

    @SneakyThrows
    @Override
    protected void putField(Float value) {
        if (primitive) {
            field.setFloat(null, value);
        } else {
            field.set(null, value);
        }
    }

    @Override
    protected Float getConfig() {
        try {
            return MathUtil.clamp(Float.parseFloat(property.getString()), min, max);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    protected void putConfig(Float value) {
        property.set(Float.toString(value));
    }

    @Override
    protected Float getDefault() {
        return defaultValue;
    }

    @SneakyThrows
    @Override
    public boolean validateField() {
        if (!primitive && field.get(null) == null) {
            ConfigValidationFailureEvent.fieldIsNull(field, -1);
            return false;
        }
        if (hasMinMax) {
            val value = (float) getField();
            if (value >= min && value <= max) {
                return true;
            }
            ConfigValidationFailureEvent.postNumericRangeOutOfBounds(field, -1, Float.toString(value),
                                                                     Float.toString(min), Float.toString(max));
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void transmit(DataOutput output) throws IOException {
        output.writeFloat(getField());
    }

    @Override
    public void receive(DataInput input) throws IOException {
        putField(input.readFloat());
    }
}
