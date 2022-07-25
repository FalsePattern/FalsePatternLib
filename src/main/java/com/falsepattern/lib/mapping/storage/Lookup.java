package com.falsepattern.lib.mapping.storage;

import com.falsepattern.lib.mapping.types.MappingType;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.val;

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

    public boolean contains(T value) {
        return values.contains(value);
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

    private static <T> void removeFirst(Iterator<Map.Entry<String, T>> iterator, T value) {
        while (iterator.hasNext()) {
            val entry = iterator.next();
            if (entry.getValue().equals(value)) {
                iterator.remove();
                return;
            }
        }
    }

    public static class LookupException extends Exception {
        public LookupException(String message) {
            super(message);
        }
    }
}
