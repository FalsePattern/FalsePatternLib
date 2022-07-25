package com.falsepattern.lib.reflection;

import com.falsepattern.lib.reflection.types.MappingType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;

@Accessors(fluent = true)
@ToString
@EqualsAndHashCode
public class UniversalMethod {
    @Getter
    public final MappedString name;
    @Getter
    public final MappedString descriptor;

    public UniversalMethod(String[] names, Map<String, String> stringPool) {
        name = new MappedString(names, 0, 2, (str) -> str.substring(str.lastIndexOf('/') + 1), stringPool);
        descriptor = new MappedString(names, 1, 2, (str) -> str, stringPool);
    }

    public String getName(@NonNull MappingType mappingType) {
        return name.get(mappingType);
    }

    public String getDescriptor(@NonNull MappingType mappingType) {
        return descriptor.get(mappingType);
    }
}
