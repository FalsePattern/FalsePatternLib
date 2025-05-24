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
package com.falsepattern.lib.config;

import com.falsepattern.lib.StableAPI;
import org.intellij.lang.annotations.Language;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.BiConsumer;

/**
 * A modern configuration system to replace the old and obtuse forge config system.
 * <br>
 * Note that just annotating a configuration class with {@link Config} is not enough, you MUST also register it using
 * {@link ConfigurationManager#initialize(Class[])} or {@link ConfigurationManager#initialize(BiConsumer, Class[])}!
 * <br>
 * For each config field, the following annotations are available:
 * <br>
 * <br>
 * <code>@Default*</code> - you MUST add these annotations, they define the default value of the given config
 * (not having a default is an error.)
 * <br>
 * {@link Comment} - you SHOULD add this annotation to every config field and the config class itself, it will
 * add a comment to the config file for users.
 * <br>
 * {@link LangKey} - you MAY add this to provide localization support.
 * <br>
 * {@link Ignore} - you MUST add this annotation to additional fields you don't want put in the config file
 * <br>
 * {@link RangeInt}, {@link RangeDouble} - you SHOULD add this to Int/IntList, Double/DoubleList configs respectively
 * to define upper/lower bounds for the values.
 * <br>
 * {@link StringMaxLength} - you MAY add this to String/StringList configs if you know a definite maximum length
 * for them.
 * <br>
 * {@link Pattern} - you MAY add this to String/StringList configs to restrict the values using regex.
 * <br>
 * {@link ListMaxLength} - you MAY add this to *List configs to limit their maximum length.
 * <br>
 * {@link ListFixedLength} - you MAY add this to *List configs to lock them to a fixed length.
 * <br>
 * {@link Name} - you SHOULD add this to config properties to give them user-friendly names in the config. SHOULD be
 * "camelCase".
 * <br>
 * {@link RequiresMcRestart}, {@link RequiresWorldRestart} - You MUST add these to configs if they require a game or
 * world reload accordingly, to avoid broken state!
 * You MUST NOT add these to configs that are meant to be changed by the user while actively playing.
 * <br>
 * {@link Synchronize} - You MAY add this to the config class.
 * Allows the config to be synchronized from server to client.
 * Useful for keeping gameplay-specific state consistent.
 * <br>
 * {@link NoSync} - You MUST add this to fields that you don't want to be synchronized by {@link Synchronize}.
 */
@StableAPI(since = "0.6.0")
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config {
    /**
     * The mod id that this configuration is associated with.
     * If {@link #customPath()} is not set, this is used for the config file name (.minecraft/config/modid.cfg)
     * @see #customPath()
     */
    @StableAPI.Expose String modid();

    /**
     * Root element category, defaults to "general". MUST NOT be an empty string.
     */
    @StableAPI.Expose String category() default "general";

    /**
     * Define category migrations here.
     * Used to rename config categories without losing data.
     * @see #pathMigrations()
     */
    @StableAPI.Expose(since = "1.5.0") String[] categoryMigrations() default {};

    /**
     * A custom configuration file path. Relative to the .minecraft/config folder.
     * The .cfg is automatically appended if no file extension is provided.
     * <br>
     * Examples:
     * <br>
     * customPath = "MyMod/magic" -> .minecraft/config/MyMod/magic.cfg
     * <br>
     * customPath = "mymod.config" -> .minecraft/config/mymod.config
     * <br>
     * @see #modid()
     */
    @StableAPI.Expose(since = "1.5.0") String customPath() default "";

    /**
     * Define file migration paths here.
     * Used to relocate config files from older versions of the mod to a new location.
     * <br>
     * NOTE: This only moves the current category, so you can even use this to split up/combine files. If every
     * category from an old config file has been migrated, the old config file is automatically deleted.
     * <br>
     * Only the FIRST successful migration is executed, and no migration happens if the new config file already contains
     * the category of this config.
     * @see #customPath()
     * @see #categoryMigrations()
     */
    @StableAPI.Expose(since = "1.5.0") String[] pathMigrations() default {};

    // region Common

    /**
     * The description of the configuration or the category. Should be in the primary language of the mod.
     * @see LangKey
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface Comment {
        @StableAPI.Expose String[] value();
    }

    /**
     * The lang file key of this configuration. Used in config GUIs.
     * <br>
     * <code>{@link LangKey#value()} + ".tooltip"</code> is used for the hover tooltips. If the localization is not
     * present, defaults to {@link Comment#value()}.
     * <br>
     * If left empty. it's autogenerated from the modid, category, and config name. (config.MODID.CATEGORY.NAME)
     * <br>
     * Also applies to categories, where it gets autogenerated from the modid and category (config.MODID.CATEGORY)
     * @see Comment
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface LangKey {
        @StableAPI.Expose String value() default "";
    }

    /**
     * If you have extra fields in the config class used for anything else other than configuring, you must annotate
     * the using this so that the config engine doesn't pick them up.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Ignore {}

    /**
     * The name of this config property in the config file. If not specified, the field's name will be used instead.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Name {
        @StableAPI.Expose String value();

        /**
         * Use this to migrate from old config names. "" is resolved to the name of the field
         */
        @StableAPI.Expose(since = "1.5.0") String[] migrations() default {};
    }

    /**
     * Whether the specific configuration needs a minecraft restart to be applied.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface RequiresMcRestart {}

    /**
     * Whether the specific configuration needs a world/server rejoin to be applied.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface RequiresWorldRestart {}

    // endregion

    // region Sync

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
    @interface Synchronize {}

    /**
     * Use this to mark config fields you don't want to synchronize in a class marked with {@link Synchronize}.
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NoSync {}

    // endregion

    // region Boolean

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

    // endregion

    // region Int

    /**
     * The default value for an int field.
     * @see RangeInt
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultInt {
        @StableAPI.Expose int value();
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

    // endregion

    // region Double

    /**
     * The default value for a double field.
     * @see RangeDouble
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultDouble {
        @StableAPI.Expose double value();
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

    // endregion

    // region String

    /**
     * The default value for a String field.
     * @see StringMaxLength
     * @see Pattern
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
        @Language("RegExp")
        @StableAPI.Expose String value();
    }

    // endregion

    // region Enum

    /**
     * The default value for an Enum field.
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultEnum {
        @StableAPI.Expose String value();
    }

    // endregion

    // region Lists

    /**
     * The default value for a boolean array field.
     * @see ListFixedLength
     * @see ListMaxLength
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultBooleanList {
        @StableAPI.Expose boolean[] value();
    }

    /**
     * The default value for an int array field.
     * @see ListFixedLength
     * @see ListMaxLength
     * @see RangeInt
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultIntList {
        @StableAPI.Expose int[] value();
    }

    /**
     * The default value for a double array field.
     * @see ListFixedLength
     * @see ListMaxLength
     * @see RangeDouble
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultDoubleList {
        @StableAPI.Expose double[] value();
    }

    /**
     * The default value for a string array field.
     * @see ListFixedLength
     * @see ListMaxLength
     * @see StringMaxLength
     * @see Pattern
     */
    @StableAPI(since = "0.6.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultStringList {
        @StableAPI.Expose String[] value();
    }

    /**
     * If this annotation is present, the list in the config will be forced to have exactly the amount of elements as
     * the default value.
     * @see ListMaxLength
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ListFixedLength {}

    /**
     * This annotation limits the maximum number of elements present in an array configuration. Only effective if
     * {@link ListFixedLength} is NOT present.
     */
    @StableAPI(since = "0.10.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ListMaxLength {
        @StableAPI.Expose int value();
    }

    // endregion


}