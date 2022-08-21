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
import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.event.ConfigValidationFailureEvent;
import lombok.SneakyThrows;
import lombok.val;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;

public abstract class AListConfigField<T> extends AConfigField<T> {
    protected final boolean fixedLength;
    protected final int maxLength;

    protected AListConfigField(Field field, Configuration configuration, String category, Property.Type type)
            throws ConfigException {
        super(field, configuration, category, type, true);
        fixedLength = field.isAnnotationPresent(Config.ListFixedLength.class);
        maxLength = Optional.ofNullable(field.getAnnotation(Config.ListMaxLength.class))
                            .map(Config.ListMaxLength::value)
                            .orElse(256);
        if (maxLength < 0) {
            throw new ConfigException(
                    "Negative length list configurations are not supported!\n" + "Field name: " + field.getName() +
                    ", class: " + field.getDeclaringClass().getName());
        }
        property.setIsListLengthFixed(fixedLength);
        property.setMaxListLength(maxLength);
        property.comment += "\n[fixed length: " + (fixedLength ? "yes" : "no") + ", max length: " + maxLength + "]";
    }

    protected abstract int length(T arr);

    protected abstract T arrayCopy(T arr);

    protected abstract void transmitElements(DataOutput output, T arr) throws IOException;

    protected abstract void receiveElements(DataInput input, T arr) throws IOException;

    protected abstract T createArray(int length);


    @SuppressWarnings("unchecked")
    @SneakyThrows
    @Override
    protected T getField() {
        return arrayCopy((T) field.get(null));
    }

    @SneakyThrows
    @Override
    protected void putField(T value) {
        field.set(null, arrayCopy(value));
    }

    @Override
    public boolean validateField() {
        val f = getField();
        if (f == null) {
            ConfigValidationFailureEvent.fieldIsNull(field, -1);
            return false;
        }
        if ((fixedLength && length(getDefault()) != length(f)) || (maxLength >= 0 && length(f) > maxLength)) {
            ConfigValidationFailureEvent.postSize(field, length(f), fixedLength, maxLength, length(getDefault()));
            return false;
        }
        return true;
    }

    @Override
    public void transmit(DataOutput output) throws IOException {
        val f = getField();
        output.writeInt(length(f));
        transmitElements(output, f);
    }

    @Override
    public void receive(DataInput input) throws IOException {
        int length = input.readInt();
        if (length > maxLength || fixedLength && length(getDefault()) != length) {
            throw new IOException("Error while retrieving config value for field " + field.getName() + " in class " +
                                  field.getDeclaringClass().getName() + ":\n" + "Illegal array length received!");
        }
        val arr = createArray(length);
        receiveElements(input, arr);
    }
}
