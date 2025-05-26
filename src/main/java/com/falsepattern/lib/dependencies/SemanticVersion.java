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
package com.falsepattern.lib.dependencies;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

public class SemanticVersion extends Version {
    @Getter
    private final int majorVersion;
    @Getter
    private final int minorVersion;
    @Getter
    private final int patchVersion;
    @Getter
    private final String preRelease;
    @Getter
    private final String build;

    public SemanticVersion(int majorVersion, int minorVersion, int patchVersion, String preRelease, String build) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
        preRelease = preRelease == null ? null : preRelease.trim();
        build = build == null ? null : build.trim();
        this.preRelease = "".equals(preRelease) ? null : preRelease;
        this.build = "".equals(build) ? null : build;
    }

    public SemanticVersion(int majorVersion, int minorVersion, int patchVersion, String preRelease) {
        this(majorVersion, minorVersion, patchVersion, preRelease, null);
    }

    public SemanticVersion(int majorVersion, int minorVersion, int patchVersion) {
        this(majorVersion, minorVersion, patchVersion, null, null);
    }

    /**
     * @since 0.10.0
     */
    public SemanticVersion(int majorVersion, int minorVersion) {
        this(majorVersion, minorVersion, -1, null, null);
    }


    /**
     * @since 0.10.0
     */
    public SemanticVersion(int majorVersion) {
        this(majorVersion, -1, -1, null, null);
    }

    public static SemanticVersionBuilder builder() {
        return new SemanticVersionBuilder();
    }

    @Override
    public int compareTo(@NonNull Version o) {
        if (o instanceof ComplexVersion) {
            val result = this.compareTo(((ComplexVersion) o).versions[0]);
            if (result != 0) {
                return result;
            } else if (((ComplexVersion) o).versions.length > 1) {
                return 1;
            } else {
                return 0;
            }
        } else if (o instanceof SemanticVersion) {
            val other = (SemanticVersion) o;
            if (majorVersion != other.majorVersion) {
                return majorVersion - other.majorVersion;
            } else if (minorVersion != other.minorVersion) {
                return minorVersion - other.minorVersion;
            } else if (patchVersion != other.patchVersion) {
                return patchVersion - other.patchVersion;
            } else if (!Objects.equals(preRelease, other.preRelease)) {
                if (preRelease == null) {
                    return 1;
                } else if (other.preRelease == null) {
                    return -1;
                }
                return preRelease.compareTo(other.preRelease);
            } else {
                return 0;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        // @formatter:off
        return majorVersion + (minorVersion < 0 ? "" : "." + minorVersion) +
               (patchVersion < 0 ? "" : "." + patchVersion) + (preRelease == null ? "" : "-" + preRelease) +
               (build == null ? "" : "+" + build);
        // @formatter:on
    }

    /**
     * @since 0.10.0
     */
    public static class SemanticVersionBuilder {
        private int majorVersion;
        private int minorVersion = -1;
        private int patchVersion = -1;
        private String preRelease;
        private String build;

        @ApiStatus.Internal
        SemanticVersionBuilder() {
        }

        public SemanticVersionBuilder majorVersion(int majorVersion) {
            this.majorVersion = majorVersion;
            return this;
        }

        public SemanticVersionBuilder minorVersion(int minorVersion) {
            this.minorVersion = minorVersion;
            return this;
        }

        public SemanticVersionBuilder patchVersion(int patchVersion) {
            this.patchVersion = patchVersion;
            return this;
        }

        public SemanticVersionBuilder preRelease(String preRelease) {
            this.preRelease = preRelease;
            return this;
        }

        public SemanticVersionBuilder build(String build) {
            this.build = build;
            return this;
        }

        public SemanticVersion build() {
            return new SemanticVersion(majorVersion, minorVersion, patchVersion, preRelease, build);
        }

        @Override
        public String toString() {
            return "SemanticVersion.SemanticVersionBuilder(majorVersion="
                   + this.majorVersion
                   + ", minorVersion="
                   + this.minorVersion
                   + ", patchVersion="
                   + this.patchVersion
                   + ", preRelease="
                   + this.preRelease
                   + ", build="
                   + this.build
                   + ")";
        }
    }
}
