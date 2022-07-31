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
 *
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
    String modid();

    /**
     * Root element category, defaults to "general". You must not specify an empty string.
     */
    String category() default "general";

    /**
     * The lang file key of this configuration. Used in config GUIs.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface LangKey {
        String value();
    }

    /**
     * The description of the configuration.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Comment {
        String[] value();
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
        boolean value();
    }

    /**
     * The range of possible values an int config can have.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RangeInt {
        int min() default Integer.MIN_VALUE;

        int max() default Integer.MAX_VALUE;
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
        int value();
    }

    /**
     * The range of possible values a float config can have.
     * Notice: float configs are deprecated! Use double configs instead!
     */
    @Deprecated
    @DeprecationDetails(stableSince = "0.6.0",
                        deprecatedSince = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RangeFloat {
        float min() default -Float.MAX_VALUE;

        float max() default Float.MAX_VALUE;
    }

    /**
     * The default value for a float field. Not having a default is deprecated since 0.10, and will be strongly
     * enforced in 0.11+!
     * Notice: float configs are deprecated! Use double configs instead!
     */
    @Deprecated
    @DeprecationDetails(stableSince = "0.6.0",
                        deprecatedSince = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultFloat {
        float value();
    }

    /**
     * The range of possible values a double config can have.
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RangeDouble {
        double min() default -Double.MAX_VALUE;

        double max() default Double.MAX_VALUE;
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
        double value();
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
        String value();
    }


    /**
     * This annotation limits the maximum number of characters present in a string configuration.
     *
     * Note: If this annotation is not present, the maximum length will be implicitly set to 256 to avoid malicious
     * synchronizations that would make clients run out of memory!
     *
     * When used with a string list, this limit will apply to each element individually, not to the size of the list as a whole.
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface StringMaxLength {
        int value();
    }

    /**
     * A regex pattern for restricting the allowed strings.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Pattern {
        String value();
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
        String value();
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
        String[] value();
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
        double[] value();
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
        int[] value();
    }

    /**
     * The default value for an boolean array field. Not having a default is deprecated since 0.10, and will be strongly
     * enforced in 0.11+!
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultBooleanList {
        boolean[] value();
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
     *
     * Note: If this annotation is not present, the maximum length will be implicitly set to 256 to avoid malicious
     * synchronizations that would make clients run out of memory!
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ListMaxLength {
        int value();
    }



    /**
     * The name of this config property in the config file. If not specified, the field's name will be used instead.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Name {
        String value();
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
     *
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