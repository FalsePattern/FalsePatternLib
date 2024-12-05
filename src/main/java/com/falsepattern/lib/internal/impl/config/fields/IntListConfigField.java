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
import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.event.ConfigValidationFailureEvent;
import com.falsepattern.lib.internal.impl.config.ConfigFieldParameters;
import lombok.val;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public class IntListConfigField extends AListConfigField<int[]> {
    private final int min;
    private final int max;
    private final int[] defaultValue;

    public IntListConfigField(ConfigFieldParameters params) throws ConfigException {
        super(params, Property.Type.INTEGER);
        val range = Optional.ofNullable(field.getAnnotation(Config.RangeInt.class));
        min = range.map(Config.RangeInt::min).orElse(Integer.MIN_VALUE);
        max = range.map(Config.RangeInt::max).orElse(Integer.MAX_VALUE);
        defaultValue = Optional.ofNullable(field.getAnnotation(Config.DefaultIntList.class))
                               .map(Config.DefaultIntList::value)
                               .orElseThrow(() -> noDefault(field, "DefaultIntList"));
        property.setDefaultValues(defaultValue);
        property.setMinValue(min);
        property.setMaxValue(max);
        if (!property.isIntList()) {
            setToDefault();
        }
        property.comment += "\n[range: " + min + " ~ " + max + ", default: " + Arrays.toString(defaultValue) + "]";
    }

    @Override
    protected int length(int[] arr) {
        return arr.length;
    }

    @Override
    protected int[] arrayCopy(int[] arr) {
        return Arrays.copyOf(arr, arr.length);
    }

    @Override
    protected void transmitElements(DataOutput output, int[] arr) throws IOException {
        for (val i : arr) {
            output.writeInt(i);
        }
    }

    @Override
    protected void receiveElements(DataInput input, int[] arr) throws IOException {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = input.readInt();
        }
    }

    @Override
    protected int[] createArray(int length) {
        return new int[length];
    }

    @Override
    public boolean validateField() {
        if (!super.validateField()) {
            return false;
        }
        int[] ints = getField();
        boolean valid = true;
        for (int j = 0; j < ints.length; j++) {
            int i = ints[j];
            if (i < min || i > max) {
                ConfigValidationFailureEvent.postNumericRangeOutOfBounds(field,
                                                                         j,
                                                                         Integer.toString(i),
                                                                         Integer.toString(min),
                                                                         Integer.toString(max));
                valid = false;
            }
        }
        return valid;
    }

    @Override
    protected int[] getConfig() {
        return property.getIntList();
    }

    @Override
    protected void putConfig(int[] value) {
        property.set(value);
    }

    @Override
    protected int[] getDefault() {
        return Arrays.copyOf(defaultValue, defaultValue.length);
    }
}
