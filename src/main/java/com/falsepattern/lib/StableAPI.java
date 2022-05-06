package com.falsepattern.lib;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Anything annotated with this annotation will be considered stable, and the class/method/field will not get breaking
 * changes without a full deprecation cycle.
 *
 * If a class or method is NOT annotated with this annotation, it will be considered unstable, and the package/method arguments/etc.
 * can freely change between patch releases without notice.
 *
 * If a class is annotated with this annotation, all currently existing public and protected members in the class will
 * be considered stable.
 *
 * Private members will never be considered stable, and can be removed without notice.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@StableAPI(since = "0.6.0")
public @interface StableAPI {
    String since();
}
