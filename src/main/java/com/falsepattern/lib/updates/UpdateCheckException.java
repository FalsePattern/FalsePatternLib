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
package com.falsepattern.lib.updates;

import com.falsepattern.lib.StableAPI;

/**
 * Exception only thrown by methods of {@link UpdateChecker}.
 * Please don't throw this in your own code, only catch/handle it.
 */
@StableAPI(since = "0.8.3")
public class UpdateCheckException extends Exception {
    @StableAPI.Internal
    public UpdateCheckException(String message) {
        super(message);
    }

    @StableAPI.Internal
    public UpdateCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    @StableAPI.Internal
    public UpdateCheckException(Throwable cause) {
        super(cause);
    }

    @StableAPI.Internal
    protected UpdateCheckException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
