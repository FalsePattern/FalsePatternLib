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
package com.falsepattern.lib;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Anything annotated with this annotation will be considered stable, and the class/method/field will not get breaking
 * changes without a full deprecation cycle.
 * <p>
 * If a class or method is NOT annotated with this annotation, it will be considered unstable, and the package/method
 * arguments/etc. can freely change between patch releases without notice.
 * <p>
 * NOTICE: You should no longer use this annotation exclusively on classes themselves, and instead, annotate every single
 * public or protected member you want to expose as a public API!
 * <p>
 * Private members will never be considered stable, and can be removed without notice.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@StableAPI(since = "0.6.0")
public @interface StableAPI {
    /**
     * The version this API was introduced/stabilized in. Used for library version tracking.
     */
    @StableAPI(since = "0.6.0")
    String since();
}
