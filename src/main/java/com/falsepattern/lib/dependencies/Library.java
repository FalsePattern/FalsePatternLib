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
import lombok.NonNull;

@StableAPI(since = "0.10.0")
public class Library {
    @StableAPI.Expose
    @NonNull
    public final String loadingModId;
    @StableAPI.Expose
    @NonNull
    public final String groupId;
    @StableAPI.Expose
    @NonNull
    public final String artifactId;
    @StableAPI.Expose
    @NonNull
    public final Version minVersion;
    @StableAPI.Expose
    public final Version maxVersion;
    @StableAPI.Expose
    @NonNull
    public final Version preferredVersion;
    @StableAPI.Expose
    public final String regularSuffix;
    @StableAPI.Expose
    public final String devSuffix;

    @StableAPI.Expose
    public Library(@NonNull String loadingModId, @NonNull String groupId, @NonNull String artifactId, @NonNull Version minVersion, Version maxVersion, @NonNull Version preferredVersion, String regularSuffix, String devSuffix) {
        this.loadingModId = loadingModId;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.preferredVersion = preferredVersion;
        this.regularSuffix = regularSuffix;
        this.devSuffix = devSuffix;
    }

    @StableAPI.Expose
    public static LibraryBuilder builder() {
        return new LibraryBuilder();
    }

    @StableAPI(since = "0.10.0")
    public static class LibraryBuilder {
        private String loadingModId;
        private String groupId;
        private String artifactId;
        private Version minVersion;
        private Version maxVersion;
        private Version preferredVersion;
        private String regularSuffix;
        private String devSuffix;

        @StableAPI.Internal
        LibraryBuilder() {
        }

        @StableAPI.Expose
        public LibraryBuilder loadingModId(@NonNull String loadingModId) {
            this.loadingModId = loadingModId;
            return this;
        }

        @StableAPI.Expose
        public LibraryBuilder groupId(@NonNull String groupId) {
            this.groupId = groupId;
            return this;
        }

        @StableAPI.Expose
        public LibraryBuilder artifactId(@NonNull String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        @StableAPI.Expose
        public LibraryBuilder minVersion(@NonNull Version minVersion) {
            this.minVersion = minVersion;
            return this;
        }

        @StableAPI.Expose
        public LibraryBuilder maxVersion(Version maxVersion) {
            this.maxVersion = maxVersion;
            return this;
        }

        @StableAPI.Expose
        public LibraryBuilder preferredVersion(@NonNull Version preferredVersion) {
            this.preferredVersion = preferredVersion;
            return this;
        }

        @StableAPI.Expose
        public LibraryBuilder regularSuffix(String regularSuffix) {
            this.regularSuffix = regularSuffix;
            return this;
        }

        @StableAPI.Expose
        public LibraryBuilder devSuffix(String devSuffix) {
            this.devSuffix = devSuffix;
            return this;
        }

        @StableAPI.Expose
        public Library build() {
            return new Library(loadingModId,
                               groupId,
                               artifactId,
                               minVersion,
                               maxVersion,
                               preferredVersion,
                               regularSuffix,
                               devSuffix);
        }

        @Override
        public String toString() {
            return "Library.LibraryBuilder(loadingModId="
                   + this.loadingModId
                   + ", groupId="
                   + this.groupId
                   + ", artifactId="
                   + this.artifactId
                   + ", minVersion="
                   + this.minVersion
                   + ", maxVersion="
                   + this.maxVersion
                   + ", preferredVersion="
                   + this.preferredVersion
                   + ", regularSuffix="
                   + this.regularSuffix
                   + ", devSuffix="
                   + this.devSuffix
                   + ")";
        }
    }
}
