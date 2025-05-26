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
package com.falsepattern.lib.mapping.storage;

import com.falsepattern.lib.mapping.types.MappingType;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.val;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Accessors(fluent = true)
@EqualsAndHashCode
public class Lookup<T> {
    private final Set<T> values = new HashSet<>();
    private final Map<String, T> notch = new HashMap<>();
    private final Map<String, T> srg = new HashMap<>();
    private final Map<String, T> mcp = new HashMap<>();

    private static <T> void removeFirst(Iterator<Map.Entry<String, T>> iterator, T value) {
        while (iterator.hasNext()) {
            val entry = iterator.next();
            if (entry.getValue().equals(value)) {
                iterator.remove();
                return;
            }
        }
    }

    public boolean contains(T value) {
        return values.contains(value);
    }

    public boolean containsKey(MappingType mappingType, String key) {
        switch (mappingType) {
            case Notch:
                return notch.containsKey(key);
            case SRG:
                return srg.containsKey(key);
            case MCP:
                return mcp.containsKey(key);
            default:
                throw new IllegalArgumentException("Invalid enum value " + mappingType);
        }
    }

    public void unwrap(@NonNull MappedString mappedString, @NonNull T value) {
        if (contains(value)) {
            //Collision avoidance.
            values.remove(value);
            removeFirst(notch.entrySet().iterator(), value);
            removeFirst(srg.entrySet().iterator(), value);
            removeFirst(mcp.entrySet().iterator(), value);
        }
        values.add(value);
        notch.put(mappedString.notch, value);
        srg.put(mappedString.srg, value);
        mcp.put(mappedString.mcp, value);
    }

    public T get(MappingType mappingType, String key) throws LookupException {
        T result;
        switch (mappingType) {
            case Notch:
                result = notch.get(key);
                break;
            case SRG:
                result = srg.get(key);
                break;
            case MCP:
                result = mcp.get(key);
                break;
            default:
                throw new IllegalArgumentException("Invalid enum value " + mappingType);
        }
        if (result == null) {
            throw new LookupException("No such key " + key + " in " + mappingType);
        }
        return result;
    }

    public static class LookupException extends Exception {
        @ApiStatus.Internal
        public LookupException(String message) {
            super(message);
        }
    }
}
