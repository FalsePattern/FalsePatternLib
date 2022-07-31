package com.falsepattern.lib;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used together with {@link Deprecated} to specify when an API was marked stable, and when it was marked for deprecation.
 * Deprecated classes MAY be removed after a full deprecation cycle as described inside the {@link StableAPI} javadoc.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@StableAPI(since = "0.10.0")
public @interface DeprecationDetails {
    @StableAPI(since = "0.10.0") String stableSince() default "";

    @StableAPI(since = "0.10.0") String deprecatedSince();
}
