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
package com.falsepattern.lib;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DEPRECATED: Use the jetbrains annotations/jspecify instead!
 * <p>
 * Anything annotated with this annotation will be considered stable, and the class/method/field will not get breaking
 * changes without a full deprecation cycle.
 * <p>
 * If a class or method is NOT annotated with this annotation, it will be considered unstable, and the package/method
 * arguments/etc. can freely change between patch releases without notice.
 * <p>
 * If you have a class implementing another class, you don't need to annotate the {@link Override}-annotated methods with
 * StableAPI, because the StableAPI system conforms to inheritance. You should still annotate the class itself, or any extra
 * fields or non-override methods you may have added if you want to expose those too.
 * <p>
 * NOTICE: You should no longer use this annotation exclusively on classes themselves, and instead, annotate every single
 * public or protected member you want to expose as a public API! See the {@link Expose} and {@link Internal} annotations
 * for extra info.
 * <p>
 * Private members will never be considered stable, and can be removed without notice.
 * <p>
 * You may set the {@link #since()} attribute to "__INTERNAL__", this will signal that even though the specific class/member
 * has been marked as stable, it is still for internal use only. This may be done for reference purposes in multi-developer
 * projects, where you need to communicate intent even in internal code.
 * <p>
 * Similarly, you may also set the {@link #since()} attribute to "__EXPERIMENTAL__", this will signal that the given API
 * is highly experimental and may be removed or change at any time, however, using it in third party projects is not an
 * error, unlike __INTERNAL__, but if any issues arise from the usage of the API or the API changing, the API developer
 * shall not be blamed for it.
 * @deprecated since 1.7.0
 * @since 0.6.0
 */
@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StableAPI {
    /**
     * The version this API was introduced/stabilized in. Used for library version tracking.
     */
    String since();

    /**
     * DEPRECATED: Use the jetbrains annotations/jspecify instead!
     * <p>
     * You may use this annotation if you want a member to have an equal effective {@link #since()} value as its owner
     * class.
     * <p>
     * Everything else from the {@link StableAPI} class still applies, this is only here for brevity's sake.
     * Note that if you add a method/field in a version newer than the class, you must also specify the correct version!
     * <p>
     * Also note that this only works for class members (methods and fields),
     * inner classes still need to use {@link StableAPI}.
     *
     * @deprecated since 1.7.0
     * @since 0.10.0
     */
    @Deprecated
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
    @interface Expose {
        String since() default "__PARENT__";
    }

    /**
     * DEPRECATED: Use the jetbrains annotations/jspecify instead!
     * <p>
     * Use this if you want to explicitly mark specific members of a {@link StableAPI} class as internal-use only.
     * <p>
     * Library consumers should never use class members marked with this annotation, as said members can be freely
     * changed or removed in any version in the library without prior notice.
     *
     * @deprecated since 1.7.0
     * @since 0.10.0
     */
    @Deprecated
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
    @interface Internal {}
}
