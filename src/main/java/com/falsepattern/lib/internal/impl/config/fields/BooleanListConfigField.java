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
import com.falsepattern.lib.config.ConfigException;
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

public class BooleanListConfigField extends AListConfigField<boolean[], Config.DefaultBooleanList> {

    public BooleanListConfigField(ConfigFieldParameters params) throws ConfigException {
        super(params,
              Property.Type.BOOLEAN,
              Config.DefaultBooleanList.class,
              Config.DefaultBooleanList::value,
              Property::setDefaultValues
              );
        val property = getProperty();
        if (!property.isBooleanList()) {
            setToDefault();
        }
        property.comment += "\n[default: " + Arrays.toString(defaultValue) + "]";
    }

    @Override
    protected int length(boolean[] arr) {
        return arr.length;
    }

    @Override
    protected boolean[] arrayCopy(boolean[] arr) {
        return Arrays.copyOf(arr, arr.length);
    }

    @Override
    protected void transmitElements(DataOutput output, boolean[] arr) throws IOException {
        val transfer = new byte[(arr.length + 7) >>> 3];
        for (int i = 0; i < arr.length; i++) {
            transfer[i >>> 3] |= arr[i] ? 1 << (i & 0x7) : 0;
        }
        for (val i : transfer) {
            output.writeByte(i);
        }
    }

    @Override
    protected void receiveElements(DataInput input, boolean[] arr) throws IOException {
        val transfer = new byte[(arr.length + 7) >>> 3];
        for (int i = 0; i < transfer.length; i++) {
            transfer[i] = input.readByte();
        }
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (transfer[i >>> 3] & (1 << (i & 0x7))) != 0;
        }
    }

    @Override
    protected boolean[] createArray(int length) {
        return new boolean[length];
    }

    @Override
    protected boolean[] getConfig() {
        return getProperty().getBooleanList();
    }

    @Override
    protected void putConfig(boolean[] value) {
        getProperty().set(value);
    }

    @Override
    protected boolean[] getDefault() {
        return Arrays.copyOf(defaultValue, defaultValue.length);
    }
}
