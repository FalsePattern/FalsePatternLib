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
import com.falsepattern.lib.config.event.ConfigValidationFailureEvent;
import com.falsepattern.lib.internal.impl.config.ConfigFieldParameters;
import lombok.SneakyThrows;
import lombok.val;

import net.minecraftforge.common.config.Property;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class AListConfigField<T, E extends Annotation> extends AConfigField<T> {
    protected final boolean fixedLength;
    protected final int maxLength;
    protected final T defaultValue;


    protected AListConfigField(ConfigFieldParameters params,
                               Property.Type type,
                               Class<E> defaultAnnotation,
                               Function<E, T> defaultValueGetter,
                               BiConsumer<Property, T> defaultValueSetter
                               )
            throws ConfigException {
        super(params, type, true);
        fixedLength = field.isAnnotationPresent(Config.ListFixedLength.class);
        val property = getProperty();
        property.setIsListLengthFixed(fixedLength);
        defaultValue = Optional.ofNullable(field.getAnnotation(defaultAnnotation))
                               .map(defaultValueGetter)
                               .orElseThrow(() -> noDefault(field, defaultAnnotation.getSimpleName()));
        defaultValueSetter.accept(property, defaultValue);
        if (fixedLength) {
            maxLength = Array.getLength(defaultValue);
        } else {
            maxLength = Optional.ofNullable(field.getAnnotation(Config.ListMaxLength.class))
                                .map(Config.ListMaxLength::value)
                                .orElse(-1);
        }
        property.setMaxListLength(maxLength);
        if (fixedLength) {
            property.comment += "\n[fixed length: " + maxLength + "]";
        } else if (maxLength >= 0) {
            property.comment += "\n[max length: " + maxLength + "]";
        }
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
        if ((fixedLength && length(getDefault()) != length) || (maxLength >= 0 && length > maxLength) || length < 0) {
            throw new IOException("Error while retrieving config value for field "
                                  + field.getName()
                                  + " in class "
                                  + field.getDeclaringClass().getName()
                                  + ":\n"
                                  + "Illegal array length received!");
        }
        val arr = createArray(length);
        receiveElements(input, arr);
    }
}
