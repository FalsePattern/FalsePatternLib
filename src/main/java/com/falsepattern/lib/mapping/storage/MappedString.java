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
package com.falsepattern.lib.mapping.storage;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.mapping.types.MappingType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.function.Function;

@Accessors(fluent = true)
@ToString
@EqualsAndHashCode
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@StableAPI(since = "0.10.0")
public class MappedString {
    @StableAPI.Expose
    public final String notch;

    @StableAPI.Expose
    public final String srg;

    @StableAPI.Expose
    public final String mcp;

    @StableAPI.Expose
    public MappedString(String[] source, int offset, int stride, Function<String, String> remapper, Map<String, String> stringPool) {
        notch = stringPool.computeIfAbsent(remapper.apply(source[offset]), (str) -> str);
        srg = stringPool.computeIfAbsent(remapper.apply(source[offset + stride]), (str) -> str);
        mcp = stringPool.computeIfAbsent(remapper.apply(source[offset + stride * 2]), (str) -> str);
    }

    @StableAPI.Expose
    public static MappedString fuse(MappedString a, MappedString b, String delimiter, Map<String, String> stringPool) {
        return new MappedString(stringPool.computeIfAbsent(a.notch + delimiter + b.notch, (str) -> str),
                                stringPool.computeIfAbsent(a.srg + delimiter + b.srg, (str) -> str),
                                stringPool.computeIfAbsent(a.mcp + delimiter + b.mcp, (str) -> str));
    }

    @StableAPI.Expose
    public String get(MappingType type) {
        switch (type) {
            case Notch:
                return notch;
            case SRG:
                return srg;
            case MCP:
                return mcp;
            default:
                throw new IllegalArgumentException("Invalid enum value " + type);
        }
    }
}
