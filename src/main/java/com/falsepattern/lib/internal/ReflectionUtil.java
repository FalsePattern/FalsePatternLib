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
package com.falsepattern.lib.internal;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ReflectionUtil {
    private static final Field f_modifiers;

    static {
        try {
            f_modifiers = getModifiersField();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        f_modifiers.setAccessible(true);
    }

    // ref: https://github.com/prestodb/presto/pull/15240/files#diff-8bf996e5c1d4fb088b84ae0528bc42686b0724bcf5a2692a1e7b5eff32c90cce
    private static Field getModifiersField() throws NoSuchFieldException
    {
        try {
            return Field.class.getDeclaredField("modifiers");
        }
        catch (NoSuchFieldException e) {
            try {
                Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
                getDeclaredFields0.setAccessible(true);
                Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
                for (Field field : fields) {
                    if ("modifiers".equals(field.getName())) {
                        return field;
                    }
                }
            }
            catch (ReflectiveOperationException ex) {
                e.addSuppressed(ex);
            }
            throw e;
        }
    }

    @SneakyThrows
    public static void jailBreak(Field field) {
        field.setAccessible(true);
        f_modifiers.set(field, field.getModifiers() & ~Modifier.FINAL);
    }

    public static void jailBreak(Method method) {
        method.setAccessible(true);
    }

    @SneakyThrows
    public static Class<?> getCallerClass() {
        return Class.forName(Thread.currentThread().getStackTrace()[3].getClassName());
    }
}
