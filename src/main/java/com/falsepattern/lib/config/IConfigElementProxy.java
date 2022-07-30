/**
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
package com.falsepattern.lib.config;

import cpw.mods.fml.client.config.ConfigGuiType;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.GuiEditArrayEntries;
import cpw.mods.fml.client.config.IConfigElement;
import java.util.List;
import java.util.regex.Pattern;

public class IConfigElementProxy<T> implements IConfigElement<T> {
    private final IConfigElement<T> proxied;
    private final Runnable onUpdate;

    public IConfigElementProxy(IConfigElement<T> proxied, Runnable onUpdate) {
        this.proxied = proxied;
        this.onUpdate = onUpdate;
    }

    @Override
    public boolean isProperty() {
        return proxied.isProperty();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class<? extends GuiConfigEntries.IConfigEntry> getConfigEntryClass() {
        return proxied.getConfigEntryClass();
    }

    @Override
    public Class<? extends GuiEditArrayEntries.IArrayEntry> getArrayEntryClass() {
        return proxied.getArrayEntryClass();
    }

    @Override
    public String getName() {
        return proxied.getName();
    }

    @Override
    public String getQualifiedName() {
        return proxied.getQualifiedName();
    }

    @Override
    public String getLanguageKey() {
        return proxied.getLanguageKey();
    }

    @Override
    public String getComment() {
        return proxied.getComment();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<IConfigElement> getChildElements() {
        return proxied.getChildElements();
    }

    @Override
    public ConfigGuiType getType() {
        return proxied.getType();
    }

    @Override
    public boolean isList() {
        return proxied.isList();
    }

    @Override
    public boolean isListLengthFixed() {
        return proxied.isListLengthFixed();
    }

    @Override
    public int getMaxListLength() {
        return proxied.getMaxListLength();
    }

    @Override
    public boolean isDefault() {
        return proxied.isDefault();
    }

    @Override
    public Object getDefault() {
        return proxied.getDefault();
    }

    @Override
    public Object[] getDefaults() {
        return proxied.getDefaults();
    }

    @Override
    public void setToDefault() {
        proxied.setToDefault();
    }

    @Override
    public boolean requiresWorldRestart() {
        return proxied.requiresWorldRestart();
    }

    @Override
    public boolean showInGui() {
        return proxied.showInGui();
    }

    @Override
    public boolean requiresMcRestart() {
        return proxied.requiresMcRestart();
    }

    @Override
    public Object get() {
        return proxied.get();
    }

    @Override
    public Object[] getList() {
        return proxied.getList();
    }

    @Override
    public void set(T value) {
        proxied.set(value);
        onUpdate.run();
    }

    @Override
    public void set(T[] aVal) {
        proxied.set(aVal);
        onUpdate.run();
    }

    @Override
    public String[] getValidValues() {
        return proxied.getValidValues();
    }

    @Override
    public T getMinValue() {
        return proxied.getMinValue();
    }

    @Override
    public T getMaxValue() {
        return proxied.getMaxValue();
    }

    @Override
    public Pattern getValidationPattern() {
        return proxied.getValidationPattern();
    }
}
