package com.falsepattern.lib.reflection.storage;

import com.falsepattern.lib.reflection.types.MappingType;
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
public class MappedString {
    public final String notch;
    public final String srg;
    public final String mcp;
    public MappedString(String[] source, int offset, int stride, Function<String, String> remapper, Map<String, String> stringPool) {
        notch = stringPool.computeIfAbsent(remapper.apply(source[offset             ]), (str) -> str);
        srg   = stringPool.computeIfAbsent(remapper.apply(source[offset + stride    ]), (str) -> str);
        mcp   = stringPool.computeIfAbsent(remapper.apply(source[offset + stride * 2]), (str) -> str);
    }

    public static MappedString fuse(MappedString a, MappedString b, String delimiter, Map<String, String> stringPool) {
        return new MappedString(stringPool.computeIfAbsent(a.notch + delimiter + b.notch, (str) -> str),
                                stringPool.computeIfAbsent(a.srg   + delimiter + b.srg  , (str) -> str),
                                stringPool.computeIfAbsent(a.mcp   + delimiter + b.mcp  , (str) -> str));
    }

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
