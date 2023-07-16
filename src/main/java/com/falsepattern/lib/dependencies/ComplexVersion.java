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
package com.falsepattern.lib.dependencies;

import com.falsepattern.lib.StableAPI;
import lombok.NonNull;
import lombok.val;

import java.util.Arrays;
import java.util.stream.Collectors;

@StableAPI(since = "0.6.0")
public class ComplexVersion extends Version {
    final Version[] versions;

    @StableAPI.Expose
    public ComplexVersion(@NonNull Version mainVersion, Version... subVersions) {
        this.versions = new Version[subVersions.length + 1];
        this.versions[0] = mainVersion;
        System.arraycopy(subVersions, 0, this.versions, 1, subVersions.length);
    }

    @Override
    public int compareTo(@NonNull Version o) {
        if (o instanceof ComplexVersion) {
            val other = (ComplexVersion) o;
            int count = Math.min(versions.length, other.versions.length);
            for (int i = 0; i < count; i++) {
                val result = versions[i].compareTo(other.versions[i]);
                if (result != 0) {
                    return result;
                }
            }
            if (versions.length != other.versions.length) {
                return versions.length - other.versions.length;
            } else {
                return 0;
            }
        } else if (o instanceof SemanticVersion) {
            val other = (SemanticVersion) o;
            val result = versions[0].compareTo(other);
            if (result != 0) {
                return result;
            }
            if (versions.length > 1) {
                return -1;
            }
            return 0;
        } else {
            throw new IllegalArgumentException("Could not compare version with class " + o.getClass().getName());
        }
    }

    @Override
    public String toString() {
        return Arrays.stream(versions).map(Version::toString).collect(Collectors.joining("-"));
    }
}
