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

package com.falsepattern.lib.internal.impl.dependencies;

import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class DepRoot {
    private String source;
    @Expose
    private Integer minJava;
    @Expose
    private Integer maxJava;
    @Expose
    private List<String> bundledArtifacts;
    @Expose
    private List<String> repositories;
    @Expose
    private Dependencies dependencies;
    @Expose
    private Dependencies modDependencies;

    @Data
    @Accessors(fluent = true)
    public static class Dependencies {
        @Expose
        private SidedDependencies always;
        @Expose
        private SidedDependencies obf;
        @Expose
        private SidedDependencies dev;
    }

    @Data
    @Accessors(fluent = true)
    public static class SidedDependencies {
        @Expose
        private List<String> common;
        @Expose
        private List<String> client;
        @Expose
        private List<String> server;
    }
}
