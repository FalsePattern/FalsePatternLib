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
package com.falsepattern.lib.config.event;

import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.internal.EventUtil;
import com.falsepattern.lib.internal.FPLog;
import com.falsepattern.lib.text.FormattedText;
import com.falsepattern.lib.toasts.GuiToast;
import com.falsepattern.lib.toasts.SimpleToast;
import com.falsepattern.lib.toasts.icon.ToastBG;
import lombok.val;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.EnumChatFormatting;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.lang.reflect.Field;

public class ConfigValidationFailureEvent extends Event {
    public final String reason;
    public final Class<?> configClass;
    public final String fieldName;
    public final boolean listElement;
    public final int listIndex;

    @ApiStatus.Internal
    protected ConfigValidationFailureEvent(String reason, Class<?> configClass, String fieldName, boolean listElement, int listIndex) {
        this.reason = reason;
        this.configClass = configClass;
        this.fieldName = fieldName;
        this.listElement = listElement;
        this.listIndex = listIndex;
    }

    @ApiStatus.Internal
    public static void postNumericRangeOutOfBounds(Field field, int listIndex, String value, String min, String max) {
        EventUtil.postOnCommonBus(new NumericRangeOutOfBounds(field.getDeclaringClass(),
                                                              field.getName(),
                                                              listIndex,
                                                              value,
                                                              min,
                                                              max));
    }

    @ApiStatus.Internal
    public static void postSize(Field field, int requestedSize, boolean fixedSize, int maxSize, int defaultSize) {
        EventUtil.postOnCommonBus(new ListSizeOutOfBounds(field.getDeclaringClass(),
                                                          field.getName(),
                                                          requestedSize,
                                                          fixedSize,
                                                          maxSize,
                                                          defaultSize));
    }

    @ApiStatus.Internal
    public static void postStringSizeOutOfBounds(Field field, int listIndex, String text, int maxSize) {
        EventUtil.postOnCommonBus(new StringSizeOutOfBounds(field.getDeclaringClass(),
                                                            field.getName(),
                                                            listIndex,
                                                            text,
                                                            maxSize));
    }

    @ApiStatus.Internal
    public static void fieldIsNull(Field field, int listIndex) {
        EventUtil.postOnCommonBus(new FieldIsNull(field.getDeclaringClass(), field.getName(), listIndex));
    }

    @ApiStatus.Internal
    public static void postStringPatternMismatch(Field field, int listIndex, String text, String pattern) {
        EventUtil.postOnCommonBus(new StringPatternMismatch(field.getDeclaringClass(),
                                                            field.getName(),
                                                            listIndex,
                                                            text,
                                                            pattern));
    }

    @ApiStatus.Internal
    @SideOnly(Side.CLIENT)
    public void toast() {
        val ann = configClass.getAnnotation(Config.class);
        val toast = new SimpleToast(ToastBG.TOAST_DARK,
                                    null,
                                    FormattedText.parse(EnumChatFormatting.RED + "Config validation failed")
                                                 .toChatText()
                                                 .get(0),
                                    FormattedText.parse(ann.modid() + ":" + ann.category()).toChatText().get(0),
                                    false,
                                    2000);
        GuiToast.add(toast);
    }

    @ApiStatus.Internal
    protected void customText(StringBuilder b) {
    }

    @ApiStatus.Internal
    public void logWarn() {
        val errorString = new StringBuilder("Error validating configuration field!");
        errorString.append("\nReason: ").append(reason);
        errorString.append("\nClass: ").append(configClass.getName()).append("\nField: ").append(fieldName);
        if (listElement) {
            errorString.append("\nArray index: ").append(listIndex);
        }
        customText(errorString);
        for (val line : errorString.toString().split("\n")) {
            FPLog.LOG.error(line);
        }
    }

    public static final class NumericRangeOutOfBounds extends ConfigValidationFailureEvent {
        public final String value;
        public final String min;
        public final String max;

        @ApiStatus.Internal
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

    public static final class ListSizeOutOfBounds extends ConfigValidationFailureEvent {
        public final int size;
        public final boolean fixedSize;
        public final int maxSize;
        public final int defaultSize;

        @ApiStatus.Internal
        public ListSizeOutOfBounds(Class<?> configClass, String fieldName, int size, boolean fixedSize, int maxSize, int defaultSize) {
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

    public static final class StringSizeOutOfBounds extends ConfigValidationFailureEvent {
        public final String text;
        public final int maxSize;

        @ApiStatus.Internal
        public StringSizeOutOfBounds(Class<?> configClass, String fieldName, int listIndex, String text, int maxSize) {
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

    public static final class FieldIsNull extends ConfigValidationFailureEvent {
        @ApiStatus.Internal
        public FieldIsNull(Class<?> configClass, String fieldName, int listIndex) {
            super("Unexpected null", configClass, fieldName, listIndex >= 0, listIndex);
        }

        @Override
        protected void customText(StringBuilder b) {

        }
    }

    public static final class StringPatternMismatch extends ConfigValidationFailureEvent {
        public final String text;
        public final String pattern;

        @ApiStatus.Internal
        public StringPatternMismatch(Class<?> configClass, String fieldName, int listIndex, String text, String pattern) {
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
