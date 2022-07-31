/**
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 * <p>
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.lib.config.event;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.text.FormattedText;
import com.falsepattern.lib.toasts.GuiToast;
import com.falsepattern.lib.toasts.SimpleToast;
import com.falsepattern.lib.toasts.icon.ToastBG;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import net.minecraft.util.EnumChatFormatting;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.lang.reflect.Field;

@StableAPI(since = "0.10.0")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ConfigValidationFailureEvent extends Event {
    @StableAPI(since = "0.10.0")
    public final String reason;
    @StableAPI(since = "0.10.0")
    public final Class<?> configClass;
    @StableAPI(since = "0.10.0")
    public final String fieldName;
    @StableAPI(since = "0.10.0")
    public final boolean listElement;
    @StableAPI(since = "0.10.0")
    public final int listIndex;

    public static void postNumericRangeOutOfBounds(Field field, int listIndex, String value, String min, String max) {
        FMLCommonHandler.instance()
                        .bus()
                        .post(new NumericRangeOutOfBounds(field.getDeclaringClass(), field.getName(), listIndex, value,
                                                          min, max));
    }

    public static void postSize(Field field, int requestedSize, boolean fixedSize, int maxSize, int defaultSize) {
        FMLCommonHandler.instance()
                        .bus()
                        .post(new ListSizeOutOfBounds(field.getDeclaringClass(), field.getName(), requestedSize,
                                                      fixedSize, maxSize, defaultSize));
    }

    public static void postStringSizeOutOfBounds(Field field, int listIndex, String text, int maxSize) {
        FMLCommonHandler.instance()
                        .bus()
                        .post(new StringSizeOutOfBounds(field.getDeclaringClass(), field.getName(), listIndex, text,
                                                        maxSize));
    }

    public static void fieldIsNull(Field field, int listIndex) {
        FMLCommonHandler.instance().bus().post(new FieldIsNull(field.getDeclaringClass(), field.getName(), listIndex));
    }

    public static void postStringPatternMismatch(Field field, int listIndex, String text, String pattern) {
        FMLCommonHandler.instance()
                        .bus()
                        .post(new StringPatternMismatch(field.getDeclaringClass(), field.getName(), listIndex, text,
                                                        pattern));
    }

    @SideOnly(Side.CLIENT)
    public void toast() {
        val ann = configClass.getAnnotation(Config.class);
        val toast = new SimpleToast(ToastBG.TOAST_DARK, null,
                                    FormattedText.parse(EnumChatFormatting.RED + "Config validation failed")
                                                 .toChatText()
                                                 .get(0),
                                    FormattedText.parse(ann.modid() + ":" + ann.category()).toChatText().get(0), false,
                                    2000);
        GuiToast.add(toast);
    }

    protected abstract void customText(StringBuilder b);

    public void logWarn() {
        val errorString = new StringBuilder("Error validating configuration field!");
        errorString.append("\nReason: ").append(reason);
        errorString.append("\nClass: ").append(configClass.getName()).append("\nField: ").append(fieldName);
        if (listElement) {
            errorString.append("\nArray index: ").append(listIndex);
        }
        customText(errorString);
        FalsePatternLib.getLog().error(errorString.toString());
    }

    @StableAPI(since = "0.10.0")
    public static class NumericRangeOutOfBounds extends ConfigValidationFailureEvent {
        @StableAPI(since = "0.10.0")
        public final String value;
        @StableAPI(since = "0.10.0")
        public final String min;
        @StableAPI(since = "0.10.0")
        public final String max;

        public NumericRangeOutOfBounds(Class<?> configClass, String fieldName, int listIndex, String value, String min, String max) {
            super("Number range out of bounds", configClass, fieldName, listIndex >= 0, listIndex);
            this.value = value;
            this.min = min;
            this.max = max;
        }

        @Override
        protected void customText(StringBuilder b) {
            b.append("\nValue: ").append(value).append("\nMin: ").append(min).append("\nMax: ").append(max);
        }
    }

    @StableAPI(since = "0.10.0")
    public static class ListSizeOutOfBounds extends ConfigValidationFailureEvent {
        @StableAPI(since = "0.10.0")
        public final int size;
        @StableAPI(since = "0.10.0")
        public final boolean fixedSize;
        @StableAPI(since = "0.10.0")
        public final int maxSize;
        @StableAPI(since = "0.10.0")
        public final int defaultSize;

        private ListSizeOutOfBounds(Class<?> configClass, String fieldName, int size, boolean fixedSize, int maxSize, int defaultSize) {
            super("Array size out of bounds", configClass, fieldName, false, -1);
            this.size = size;
            this.fixedSize = fixedSize;
            this.maxSize = maxSize;
            this.defaultSize = defaultSize;
        }

        @Override
        protected void customText(StringBuilder b) {
            b.append("\nSize: ").append(size);
            if (fixedSize) {
                b.append("\nRequired size: ").append(defaultSize);
            }
            b.append("\nMaximum size: ").append(maxSize);
        }
    }

    @StableAPI(since = "0.10.0")
    public static class StringSizeOutOfBounds extends ConfigValidationFailureEvent {
        @StableAPI(since = "0.10.0")
        public final String text;
        @StableAPI(since = "0.10.0")
        public final int maxSize;

        private StringSizeOutOfBounds(Class<?> configClass, String fieldName, int listIndex, String text, int maxSize) {
            super("String size out of bounds", configClass, fieldName, listIndex >= 0, listIndex);
            this.text = text;
            this.maxSize = maxSize;
        }

        @Override
        protected void customText(StringBuilder b) {
            b.append("\nText: ")
             .append(text)
             .append("\nSize: ")
             .append(text.length())
             .append("\nMax size:")
             .append(maxSize);
        }
    }

    @StableAPI(since = "0.10.0")
    public static class FieldIsNull extends ConfigValidationFailureEvent {
        private FieldIsNull(Class<?> configClass, String fieldName, int listIndex) {
            super("Unexpected null", configClass, fieldName, listIndex >= 0, listIndex);
        }

        @Override
        protected void customText(StringBuilder b) {

        }
    }

    @StableAPI(since = "0.10.0")
    public static class StringPatternMismatch extends ConfigValidationFailureEvent {
        @StableAPI(since = "0.10.0")
        public final String text;
        @StableAPI(since = "0.10.0")
        public final String pattern;

        private StringPatternMismatch(Class<?> configClass, String fieldName, int listIndex, String text, String pattern) {
            super("String pattern mismatch", configClass, fieldName, listIndex >= 0, listIndex);
            this.text = text;
            this.pattern = pattern;
        }

        @Override
        protected void customText(StringBuilder b) {
            b.append("\nText: ").append(text).append("\nPattern: ").append(pattern);
        }
    }
}
