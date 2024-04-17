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
import com.falsepattern.lib.config.event.ConfigValidationFailureEvent;
import lombok.val;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public class DoubleListConfigField extends AListConfigField<double[]> {
    private final double min;
    private final double max;
    private final double[] defaultValue;

    public DoubleListConfigField(Field field, Configuration configuration, String category) throws ConfigException {
        super(field, configuration, category, Property.Type.INTEGER);
        val range = Optional.ofNullable(field.getAnnotation(Config.RangeDouble.class));
        min = range.map(Config.RangeDouble::min).orElse(-Double.MAX_VALUE);
        max = range.map(Config.RangeDouble::max).orElse(Double.MAX_VALUE);
        defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultDoubleList.class))
                               .map(Config.DefaultDoubleList::value)
                               .orElseThrow(() -> noDefault(field, "DefaultDoubleList"));
        property.setDefaultValues(defaultValue);
        property.setMinValue(min);
        property.setMaxValue(max);
        if (!property.isDoubleList()) {
            setToDefault();
        }
        property.comment += "\n[range: " + min + " ~ " + max + ", default: " + Arrays.toString(defaultValue) + "]";
    }

    @Override
    protected int length(double[] arr) {
        return arr.length;
    }

    @Override
    protected double[] arrayCopy(double[] arr) {
        return Arrays.copyOf(arr, arr.length);
    }

    @Override
    protected void transmitElements(DataOutput output, double[] arr) throws IOException {
        for (val i : arr) {
            output.writeDouble(i);
        }
    }

    @Override
    protected void receiveElements(DataInput input, double[] arr) throws IOException {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = input.readInt();
        }
    }

    @Override
    protected double[] createArray(int length) {
        return new double[length];
    }

    @Override
    public boolean validateField() {
        if (!super.validateField()) {
            return false;
        }
        double[] doubles = getField();
        boolean valid = true;
        for (int j = 0; j < doubles.length; j++) {
            double d = doubles[j];
            if (d < min || d > max) {
                ConfigValidationFailureEvent.postNumericRangeOutOfBounds(field,
                                                                         j,
                                                                         Double.toString(d),
                                                                         Double.toString(min),
                                                                         Double.toString(max));
                valid = false;
            }
        }
        return valid;
    }

    @Override
    protected double[] getConfig() {
        return property.getDoubleList();
    }

    @Override
    protected void putConfig(double[] value) {
        property.set(value);
    }

    @Override
    protected double[] getDefault() {
        return Arrays.copyOf(defaultValue, defaultValue.length);
    }
}
