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

package com.falsepattern.lib.internal.impl.config;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import cpw.mods.fml.client.config.ConfigGuiType;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.GuiEditArrayEntries;
import cpw.mods.fml.client.config.IConfigElement;

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class ConfigElementProxy<T> implements IConfigElement<T> {
    private final Supplier<Property> prop;
    private final String comment;
    private final Runnable syncCallback;

    public ConfigElementProxy(Supplier<Property> prop, String comment, Runnable syncCallback) {
        this.prop = prop;
        this.comment = comment;
        this.syncCallback = syncCallback;
    }

    private ConfigElement<T> element() {
        return new ConfigElement<>(prop.get());
    }

    @Override
    public boolean isProperty() {
        return element().isProperty();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class<? extends GuiConfigEntries.IConfigEntry> getConfigEntryClass() {
        return element().getConfigEntryClass();
    }

    @Override
    public Class<? extends GuiEditArrayEntries.IArrayEntry> getArrayEntryClass() {
        return element().getArrayEntryClass();
    }

    @Override
    public String getName() {
        return element().getName();
    }

    @Override
    public String getQualifiedName() {
        return element().getQualifiedName();
    }

    @Override
    public String getLanguageKey() {
        return element().getLanguageKey();
    }

    @Override
    public String getComment() {
        return comment;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<IConfigElement> getChildElements() {
        return element().getChildElements();
    }

    // Caller sensitive
    @Override
    public ConfigGuiType getType() {
        return element().getType();
    }

    @Override
    public boolean isList() {
        return element().isList();
    }

    @Override
    public boolean isListLengthFixed() {
        return element().isListLengthFixed();
    }

    @Override
    public int getMaxListLength() {
        return element().getMaxListLength();
    }

    @Override
    public boolean isDefault() {
        return element().isDefault();
    }

    @Override
    public Object getDefault() {
        return element().getDefault();
    }

    @Override
    public Object[] getDefaults() {
        return element().getDefaults();
    }

    @Override
    public void setToDefault() {
        element().setToDefault();
        syncCallback.run();
    }

    @Override
    public boolean requiresWorldRestart() {
        return element().requiresWorldRestart();
    }

    @Override
    public boolean showInGui() {
        return element().showInGui();
    }

    @Override
    public boolean requiresMcRestart() {
        return element().requiresMcRestart();
    }

    @Override
    public Object get() {
        return element().get();
    }

    @Override
    public Object[] getList() {
        return element().getList();
    }

    @Override
    public void set(T value) {
        element().set(value);
        syncCallback.run();
    }

    @Override
    public void set(T[] aVal) {
        element().set(aVal);
        syncCallback.run();
    }

    @Override
    public String[] getValidValues() {
        return element().getValidValues();
    }

    @Override
    public T getMinValue() {
        return element().getMinValue();
    }

    @Override
    public T getMaxValue() {
        return element().getMaxValue();
    }

    @Override
    public Pattern getValidationPattern() {
        return element().getValidationPattern();
    }
}
