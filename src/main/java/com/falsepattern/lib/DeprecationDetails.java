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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * DEPRECATED: Use the jetbrains annotations/jspecify instead!
 * <p>
 * Used together with {@link Deprecated} to specify when an API was marked stable, and when it was marked for deprecation.
 * Deprecated classes MAY be removed after a full deprecation cycle as described inside the {@link StableAPI} javadoc.
 * @since 0.10.0
 */
@Deprecated(since = "1.7.0")
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface DeprecationDetails {
    String deprecatedSince();

    /**
     * @since 0.11.0
     */
    String replacement() default "";

    /**
     * DEPRECATED: Use the jetbrains annotations/jspecify instead!
     * <p>
     * This marks an API for removal in a future version.
     *
     * @since 0.12.0
     */
    @Deprecated(since = "1.7.0")
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @interface RemovedInVersion {
        String value();
    }
}
