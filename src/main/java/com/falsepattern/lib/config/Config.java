package com.falsepattern.lib.config;

import com.falsepattern.lib.StableAPI;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A modern configuration system to replace the old and obtuse forge config system.
 *
 * Note that just annotating a configuration class with {@link Config} is not enough, you must also register it using
 * {@link com.falsepattern.lib.config.ConfigurationManager#registerConfig}!
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@StableAPI(since = "0.6.0")
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
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface LangKey {
        String value();
    }

    /**
     * The description of the configuration.
     */
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
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Ignore {
    }

    /**
     * The default value for a boolean field. Not having a default is deprecated since 0.10, and will be strongly
     * enforced in 0.11+!
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultBoolean {
        boolean value();
    }

    /**
     * The range of possible values an int config can have.
     */
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
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultInt {
        int value();
    }

    /**
     * The range of possible values a float config can have.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RangeFloat {
        float min() default Float.MIN_VALUE;

        float max() default Float.MAX_VALUE;
    }

    /**
     * The default value for a float field. Not having a default is deprecated since 0.10, and will be strongly
     * enforced in 0.11+!
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultFloat {
        float value();
    }

    /**
     * The default value for a String field. Not having a default is deprecated since 0.10, and will be strongly
     * enforced in 0.11+!
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultString {
        String value();
    }

    /**
     * A regex pattern for restricting the allowed strings.
     */
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
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultStringList {
        String[] value();
    }

    /**
     * The name of this config property in the config file. If not specified, the field's name will be used instead.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Name {
        String value();
    }

    /**
     * Whether the specific configuration needs a minecraft restart to be applied.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface RequiresMcRestart {
    }

    /**
     * Whether the specific configuration needs a world/server rejoin to be applied.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface RequiresWorldRestart {
    }
}