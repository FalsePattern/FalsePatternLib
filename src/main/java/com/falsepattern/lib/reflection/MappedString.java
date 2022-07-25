package com.falsepattern.lib.reflection;

import com.falsepattern.lib.reflection.types.MappingType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.function.Function;

@Accessors(fluent = true)
@ToString
@EqualsAndHashCode
@Getter
public class MappedString {
    public final String notch;
    public final String srg;
    public final String mcp;
    public MappedString(String[] source, int offset, int stride, Function<String, String> remapper, Map<String, String> stringPool) {
        notch = stringPool.computeIfAbsent(remapper.apply(source[offset             ]), (str) -> str);
        srg   = stringPool.computeIfAbsent(remapper.apply(source[offset + stride    ]), (str) -> str);
        mcp   = stringPool.computeIfAbsent(remapper.apply(source[offset + stride * 2]), (str) -> str);
    }

    public String get(@NonNull MappingType type) {
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
