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
public class UniversalField {
    @Getter
    public final MappedString name;

    public UniversalField(String[] names, Map<String, String> stringPool) {
        name = new MappedString(names, 0, 1, (str) -> str.substring(str.lastIndexOf('/') + 1), stringPool);
    }

    public String getName(@NonNull MappingType mappingType) {
        return name.get(mappingType);
    }
}
