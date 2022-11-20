package com.falsepattern.lib.mapping.moveme;

//TODO: Move this somewhere it makes actual sense
@FunctionalInterface
public interface ThrowingBIFunction<T, U, R, E extends Throwable> {
    R apply(T t, U u) throws E;
}
