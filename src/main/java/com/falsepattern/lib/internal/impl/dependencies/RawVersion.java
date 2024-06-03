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

package com.falsepattern.lib.internal.impl.dependencies;

import com.falsepattern.lib.dependencies.ComplexVersion;
import com.falsepattern.lib.dependencies.SemanticVersion;
import com.falsepattern.lib.dependencies.Version;
import org.jetbrains.annotations.NotNull;

public class RawVersion extends Version {
    public final String versionString;

    public RawVersion(String versionString) {
        this.versionString = versionString;
    }

    @Override
    public int compareTo(@NotNull Version o) {
        if (o instanceof RawVersion) {
            return versionString.compareTo(((RawVersion) o).versionString);
        } else if (o instanceof SemanticVersion) {
            return 1;
        } else if (o instanceof ComplexVersion) {
            return 1;
        } else {
            throw new IllegalArgumentException("Unknown version type: " + o.getClass().getName());
        }
    }

    @Override
    public String toString() {
        return versionString;
    }
}
