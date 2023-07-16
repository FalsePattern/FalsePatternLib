/*
 * Copyright (C) 2022-2023 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
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

import com.falsepattern.lib.DeprecationDetails;
import com.falsepattern.lib.StableAPI;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.BiConsumer;

/**
 * A modern configuration system to replace the old and obtuse forge config system.
 * <p>
 * Note that just annotating a configuration class with {@link Config} is not enough, you must also register it using
 * {@link ConfigurationManager#initialize(Class[])} or {@link ConfigurationManager#initialize(BiConsumer, Class[])}!
 */
@StableAPI(since = "0.6.0")
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config {
    /**
     * The mod id that this configuration is associated with.
     */
    @StableAPI.Expose String modid();

    /**
     * Root element category, defaults to "general". You must not specify an empty string.
     */
    @StableAPI.Expose String category() default "general";

    /**
     * The lang file key of this configuration. Used in config GUIs.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface LangKey {
        @StableAPI.Expose String value();
    }

    /**
     * The description of the configuration.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Comment {
        @StableAPI.Expose String[] value();
    }

    /**
     * If you have extra fields in the config class used for anything else other than configuring, you must annotate
     * the using this so that the config engine doesn't pick them up.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Ignore {
    }

    /**
     * The default value for a boolean field. Not having a default is deprecated since 0.10, and will be strongly
     * enforced in 0.11+!
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultBoolean {
        @StableAPI.Expose boolean value();
    }

    /**
     * The range of possible values an int config can have.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RangeInt {
        @StableAPI.Expose int min() default Integer.MIN_VALUE;

        @StableAPI.Expose int max() default Integer.MAX_VALUE;
    }

    /**
     * The default value for an int field. Not having a default is deprecated since 0.10, and will be strongly
     * enforced in 0.11+!
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultInt {
        @StableAPI.Expose int value();
    }

    /**
     * The range of possible values a float config can have.
     * Notice: float configs are deprecated! Use double configs instead!
     */
    @Deprecated
    @DeprecationDetails(deprecatedSince = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @StableAPI(since = "0.6.0")
    @interface RangeFloat {
        @StableAPI.Expose float min() default -Float.MAX_VALUE;

        @StableAPI.Expose float max() default Float.MAX_VALUE;
    }

    /**
     * The default value for a float field. Not having a default is deprecated since 0.10, and will be strongly
     * enforced in 0.11+!
     * Notice: float configs are deprecated! Use double configs instead!
     */
    @Deprecated
    @DeprecationDetails(deprecatedSince = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @StableAPI(since = "0.6.0")
    @interface DefaultFloat {
        @StableAPI.Expose float value();
    }

    /**
     * The range of possible values a double config can have.
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RangeDouble {
        @StableAPI.Expose double min() default -Double.MAX_VALUE;

        @StableAPI.Expose double max() default Double.MAX_VALUE;
    }

    /**
     * The default value for a double field. Not having a default is deprecated since 0.10, and will be strongly
     * enforced in 0.11+!
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultDouble {
        @StableAPI.Expose double value();
    }

    /**
     * The default value for a String field. Not having a default is deprecated since 0.10, and will be strongly
     * enforced in 0.11+!
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultString {
        @StableAPI.Expose String value();
    }


    /**
     * This annotation limits the maximum number of characters present in a string configuration.
     * <p>
     * Note: If this annotation is not present, the maximum length will be implicitly set to 256 to avoid malicious
     * synchronizations that would make clients run out of memory!
     * <p>
     * When used with a string list, this limit will apply to each element individually, not to the size of the list as a whole.
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface StringMaxLength {
        @StableAPI.Expose int value();
    }

    /**
     * A regex pattern for restricting the allowed strings.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Pattern {
        @StableAPI.Expose String value();
    }

    /**
     * The default value for an Enum field. Not having a default is deprecated since 0.10, and will be strongly
     * enforced in 0.11+!
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultEnum {
        @StableAPI.Expose String value();
    }

    /**
     * The default value for a string array field. Not having a default is deprecated since 0.10, and will be strongly
     * enforced in 0.11+!
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultStringList {
        @StableAPI.Expose String[] value();
    }

    /**
     * The default value for a double array field. Not having a default is deprecated since 0.10, and will be strongly
     * enforced in 0.11+!
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultDoubleList {
        @StableAPI.Expose double[] value();
    }

    /**
     * The default value for an int array field. Not having a default is deprecated since 0.10, and will be strongly
     * enforced in 0.11+!
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultIntList {
        @StableAPI.Expose int[] value();
    }

    /**
     * The default value for a boolean array field. Not having a default is deprecated since 0.10, and will be strongly
     * enforced in 0.11+!
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultBooleanList {
        @StableAPI.Expose boolean[] value();
    }

    /**
     * If this annotation is present, the list in the config will be forced to have exactly the amount of elements as
     * the default value.
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ListFixedLength {
    }

    /**
     * This annotation limits the maximum number of elements present in an array configuration. Only effective if
     * {@link ListFixedLength} is NOT present.
     * <p>
     * Note: If this annotation is not present, the maximum length will be implicitly set to 256 to avoid malicious
     * synchronizations that would make clients run out of memory!
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ListMaxLength {
        @StableAPI.Expose int value();
    }


    /**
     * The name of this config property in the config file. If not specified, the field's name will be used instead.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Name {
        @StableAPI.Expose String value();
    }

    /**
     * Whether the specific configuration needs a minecraft restart to be applied.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface RequiresMcRestart {
    }

    /**
     * Whether the specific configuration needs a world/server rejoin to be applied.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface RequiresWorldRestart {
    }

    /**
     * Signals that this configuration class should be synchronized between the client and the server when
     * joining a multiplayer instance.
     * <p>
     * Note that synchronization ALWAYS happens FROM the server TO the client. The server should NEVER attempt to get
     * configuration values from a client. This is to avoid malicious clients manipulating the server configs.
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Synchronize {
    }

    /**
     * Use this to mark config fields you don't want to synchronize in a class marked with {@link Synchronize}.
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NoSync {
    }
}