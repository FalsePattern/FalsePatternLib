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
package com.falsepattern.lib.dependencies;

import com.falsepattern.lib.DeprecationDetails;
import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.internal.impl.dependencies.DependencyLoaderImpl;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;


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

    @Deprecated
    @DeprecationDetails(deprecatedSince = "0.10.0")
    @StableAPI.Expose
    public static void loadLibrary(@NonNull String loadingModId, @NonNull String groupId, @NonNull String artifactId, @NonNull Version minVersion, Version maxVersion, @NonNull Version preferredVersion, String regularSuffix, String devSuffix) {
        FalsePatternLib.getLog()
                       .warn(DependencyLoader.class.getName() +
                             ".loadLibrary is deprecated and will be removed in FalsePatternLib 0.11! Use loadLibraries instead!");
        DependencyLoaderImpl.loadLibrary(loadingModId, groupId, artifactId, minVersion, maxVersion, preferredVersion,
                                         regularSuffix, devSuffix);
    }

    @DeprecationDetails(deprecatedSince = "0.10.0")
    @StableAPI.Expose
    @Deprecated
    public static VoidBuilder builder() {
        return new VoidBuilder();
    }

    @StableAPI(since = "0.6.0")
    @DeprecationDetails(deprecatedSince = "0.10.0")
    public static class VoidBuilder {
        private String loadingModId;
        private String groupId;
        private String artifactId;
        private Version minVersion;
        private Version maxVersion;
        private Version preferredVersion;
        private String regularSuffix;
        private String devSuffix;

        @Deprecated
        VoidBuilder() {
        }

        @StableAPI.Expose
        @Deprecated
        public VoidBuilder loadingModId(@NonNull String loadingModId) {
            this.loadingModId = loadingModId;
            return this;
        }

        @StableAPI.Expose
        @Deprecated
        public VoidBuilder groupId(@NonNull String groupId) {
            this.groupId = groupId;
            return this;
        }

        @StableAPI.Expose
        @Deprecated
        public VoidBuilder artifactId(@NonNull String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        @StableAPI.Expose
        @Deprecated
        public VoidBuilder minVersion(@NonNull Version minVersion) {
            this.minVersion = minVersion;
            return this;
        }

        @StableAPI.Expose
        @Deprecated
        public VoidBuilder maxVersion(Version maxVersion) {
            this.maxVersion = maxVersion;
            return this;
        }

        @StableAPI.Expose
        @Deprecated
        public VoidBuilder preferredVersion(@NonNull Version preferredVersion) {
            this.preferredVersion = preferredVersion;
            return this;
        }

        @StableAPI.Expose
        @Deprecated
        public VoidBuilder regularSuffix(String regularSuffix) {
            this.regularSuffix = regularSuffix;
            return this;
        }

        @StableAPI.Expose
        @Deprecated
        public VoidBuilder devSuffix(String devSuffix) {
            this.devSuffix = devSuffix;
            return this;
        }

        @StableAPI.Expose
        @Deprecated
        public void build() {
            DependencyLoader.loadLibrary(loadingModId, groupId, artifactId, minVersion, maxVersion, preferredVersion,
                                         regularSuffix, devSuffix);
        }

        @Override
        public String toString() {
            return "DependencyLoader.VoidBuilder(loadingModId=" + this.loadingModId + ", groupId=" + this.groupId +
                   ", artifactId=" + this.artifactId + ", minVersion=" + this.minVersion + ", maxVersion=" +
                   this.maxVersion + ", preferredVersion=" + this.preferredVersion + ", regularSuffix=" +
                   this.regularSuffix + ", devSuffix=" + this.devSuffix + ")";
        }
    }
}
