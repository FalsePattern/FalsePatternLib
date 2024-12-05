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
import com.falsepattern.lib.internal.impl.config.ConfigFieldParameters;
import com.falsepattern.lib.internal.impl.config.DeclOrderInternal;
import lombok.Getter;
import lombok.val;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;

public abstract class AConfigField<T> {
    public final boolean noSync;
    public final int order;
    public final String name;
    protected final Field field;
    protected final Configuration configuration;
    protected final String category;
    protected final String langKey;
    protected final Property.Type type;
    @Getter
    protected final Property property;
    @Getter
    protected final String comment;
    private boolean uninitialized;

    public static RuntimeException noDefault(Field field, String annotation) {
        return new RuntimeException("The field "
                                    + field.getName()
                                    + " in class "
                                    + field.getDeclaringClass().getName()
                                    + " has no "
                                    + annotation
                                    + " annotation!");
    }

    protected AConfigField(ConfigFieldParameters params, Property.Type type) {
        this(params, type, false);
    }

    protected AConfigField(ConfigFieldParameters params, Property.Type type, boolean isList) {
        this.field = params.field();
        this.configuration = params.configuration();
        this.category = params.category();
        comment = Optional.ofNullable(field.getAnnotation(Config.Comment.class))
                          .map(Config.Comment::value)
                          .map((lines) -> String.join("\n", lines))
                          .orElse("");
        val nameAnnotation = Optional.ofNullable(field.getAnnotation(Config.Name.class));
        name = nameAnnotation.map(Config.Name::value)
                             .orElse(field.getName());
        langKey =
                Optional.ofNullable(field.getAnnotation(Config.LangKey.class))
                        .map(Config.LangKey::value)
                        .map(x -> x.isEmpty() ? "config." + params.modid() + "." + params.category() + "." + name : x)
                        .orElse(name);
        this.type = type;
        val cat = configuration.getCategory(category);
        uninitialized = !cat.containsKey(name);
        if (isList) {
            property = configuration.get(category, name, new String[0], comment, type);
        } else {
            property = configuration.get(category, name, "", comment, type);
        }
        property.setLanguageKey(langKey);
        property.setRequiresWorldRestart(field.isAnnotationPresent(Config.RequiresWorldRestart.class));
        property.setRequiresMcRestart(field.isAnnotationPresent(Config.RequiresMcRestart.class));
        noSync = field.isAnnotationPresent(Config.NoSync.class);
        order = Optional.ofNullable(field.getAnnotation(DeclOrderInternal.class))
                        .map(DeclOrderInternal::value)
                        .orElse(-1);
    }

    protected abstract T getField();

    protected abstract void putField(T value);

    protected abstract T getConfig();

    protected abstract void putConfig(T value);

    protected abstract T getDefault();

    public void save() {
        if (!validateField()) {
            setToDefault();
        } else {
            putConfig(getField());
        }
    }

    public void load() {
        putField(getConfig());
        if (!validateField()) {
            setToDefault();
        }
    }

    public void setToDefault() {
        putField(getDefault());
        putConfig(getDefault());
    }

    public void init() {
        if (uninitialized) {
            uninitialized = false;
            putField(getDefault());
            putConfig(getDefault());
        } else {
            load();
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public abstract boolean validateField();

    public abstract void transmit(DataOutput output) throws IOException;

    public abstract void receive(DataInput input) throws IOException;

    public Field getJavaField() {
        return field;
    }
}
