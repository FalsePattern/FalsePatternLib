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

import org.jetbrains.annotations.ApiStatus;

/**
 * A really basic wrapper for config to simplify handling them in external code.
 */
public class ConfigException extends Exception {

    @ApiStatus.Internal
    public ConfigException(String message) {
        super(message);
    }

    @ApiStatus.Internal
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    @ApiStatus.Internal
    public ConfigException(Throwable cause) {
        super(cause);
    }
}
