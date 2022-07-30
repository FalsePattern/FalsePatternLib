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
package com.falsepattern.lib.dependencies;

import com.falsepattern.lib.StableAPI;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

@StableAPI(since = "0.6.0")
public class SemanticVersion extends Version {
    @Getter private final int majorVersion;
    @Getter private final int minorVersion;
    @Getter private final int patchVersion;
    @Getter private final String preRelease;
    @Getter private final String build;

    public SemanticVersion(int majorVersion, int minorVersion, int patchVersion, String preRelease) {
        this(majorVersion, minorVersion, patchVersion, preRelease, null);
    }

    @Builder
    public SemanticVersion(int majorVersion, int minorVersion, int patchVersion, String preRelease, String build) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
        preRelease = preRelease == null ? null : preRelease.trim();
        build = build == null ? null : build.trim();
        this.preRelease = "".equals(preRelease) ? null : preRelease;
        this.build = "".equals(build) ? null : build;
    }

    public SemanticVersion(int majorVersion, int minorVersion, int patchVersion) {
        this(majorVersion, minorVersion, patchVersion, null, null);
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
        return majorVersion + "." + minorVersion + "." + patchVersion + (preRelease == null ? "" : "-" + preRelease) +
               (build == null ? "" : "+" + build);
    }
}
