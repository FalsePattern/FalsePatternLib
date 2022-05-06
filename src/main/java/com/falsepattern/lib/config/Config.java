package com.falsepattern.lib.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config {
    /**
     * The mod id that this configuration is associated with.
     */
    String modid();

    /**
     * A user friendly name for the config file,
     * the default will be modid
     */
    String name() default "";

    /**
     * Root element category, defaults to "general", if this is an empty string then the root category is disabled.
     * Any primitive fields will cause an error, and you must specify sub-category objects
     */
    String category() default "general";

    public static enum Type {
        /**
         * Loaded once, directly after mod construction. Before pre-init.
         * This class must have static fields.
         */
        INSTANCE(true);


        private boolean isStatic = true;

        private Type(boolean isStatic) {this.isStatic = isStatic;}

        public boolean isStatic() {return this.isStatic;}
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface LangKey {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Comment {
        String[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Ignore {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RangeInt {
        int min() default Integer.MIN_VALUE;

        int max() default Integer.MAX_VALUE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RangeDouble {
        double min() default Double.MIN_VALUE;

        double max() default Double.MAX_VALUE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Name {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface RequiresMcRestart {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface RequiresWorldRestart {}
}