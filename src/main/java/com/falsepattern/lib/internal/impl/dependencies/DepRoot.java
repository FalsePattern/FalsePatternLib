/*
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

package com.falsepattern.lib.internal.impl.dependencies;

import com.falsepattern.lib.StableAPI;
import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class DepRoot {
    private String source;
    @Expose
    @StableAPI.Expose(since = "__INTERNAL__")
    private List<String> repositories;
    @Expose
    @StableAPI.Expose(since = "__INTERNAL__")
    private Dependencies dependencies;

    @Data
    @Accessors(fluent = true)
    public static class Dependencies {
        @Expose
        @StableAPI.Expose(since = "__INTERNAL__")
        private SidedDependencies always;
        @Expose
        @StableAPI.Expose(since = "__INTERNAL__")
        private SidedDependencies obf;
        @Expose
        @StableAPI.Expose(since = "__INTERNAL__")
        private SidedDependencies dev;
    }

    @Data
    @Accessors(fluent = true)
    public static class SidedDependencies {
        @Expose
        @StableAPI.Expose(since = "__INTERNAL__")
        private List<String> common;
        @Expose
        @StableAPI.Expose(since = "__INTERNAL__")
        private List<String> client;
        @Expose
        @StableAPI.Expose(since = "__INTERNAL__")
        private List<String> server;
    }
}
