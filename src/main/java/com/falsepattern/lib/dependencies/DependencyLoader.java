/*
 * This file is part of FalsePatternLib.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalsePatternLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalsePatternLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalsePatternLib. If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.lib.dependencies;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.impl.dependencies.DependencyLoaderImpl;

import java.util.concurrent.CompletableFuture;


/**
 * For regular external dependencies, see the DEPENDENCIES.MD files in the root of the resources for more information.
 * If you need more power than the one provided by the json format, you may use this class for granular loading instead.
 */
@StableAPI(since = "0.6.0")
public class DependencyLoader {
    @StableAPI.Expose
    public static void addMavenRepo(String url) {
        DependencyLoaderImpl.addMavenRepo(url);
    }

    @StableAPI.Expose(since = "0.10.0")
    public static CompletableFuture<Void> loadLibrariesAsync(Library... libraries) {
        return DependencyLoaderImpl.loadLibrariesAsync(libraries);
    }

    @StableAPI.Expose(since = "0.10.0")
    public static void loadLibraries(Library... libraries) {
        DependencyLoaderImpl.loadLibraries(libraries);
    }
}
