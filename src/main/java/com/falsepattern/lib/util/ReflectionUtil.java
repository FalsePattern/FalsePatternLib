package com.falsepattern.lib.util;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ReflectionUtil {
    private static final Field f_modifiers;
    static {
        try {
            f_modifiers = Field.class.getDeclaredField("modifiers");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        f_modifiers.setAccessible(true);
    }

    @SneakyThrows
    public static void jailBreak(Field field) {
        field.setAccessible(true);
        f_modifiers.set(field, field.getModifiers() & ~Modifier.FINAL);
    }

    public static void jailBreak(Method method) {
        method.setAccessible(true);
    }
}
