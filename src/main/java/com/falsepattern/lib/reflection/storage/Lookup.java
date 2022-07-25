package com.falsepattern.lib.reflection.storage;

import com.falsepattern.lib.reflection.MappedString;
import com.falsepattern.lib.reflection.types.MappingType;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Accessors(fluent = true)
@EqualsAndHashCode
public class Lookup<T> {
    private final Map<String, T> notch = new HashMap<>();
    private final Map<String, T> srg = new HashMap<>();
    private final Map<String, T> mcp = new HashMap<>();

    public void unwrap(MappedString mappedString, T value) {
        notch.put(mappedString.notch, value);
        srg.put(mappedString.srg, value);
        mcp.put(mappedString.mcp, value);
    }

    public T get(@NonNull MappingType mappingType, @NonNull String key) {
        switch (mappingType) {
            case Notch:
                return notch.get(key);
            case SRG:
                return srg.get(key);
            case MCP:
                return mcp.get(key);
            default:
                throw new IllegalArgumentException("Invalid enum value " + mappingType);
        }
    }
}
