package com.falsepattern.lib.reflection;

import com.falsepattern.lib.reflection.storage.Lookup;
import com.falsepattern.lib.reflection.types.MappingType;
import com.falsepattern.lib.reflection.types.NameType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;

@Accessors(fluent = true)
@ToString
@EqualsAndHashCode
public class UniversalClass {
    @Getter
    public final MappedString internalName;
    @Getter
    public final MappedString regularName;

    private final Lookup<UniversalField> fields = new Lookup<>();
    private final Lookup<UniversalMethod> methods = new Lookup<>();

    public UniversalClass(String[] names, Map<String, String> stringPool) {
        internalName = new MappedString(names, 0, 1, (str) -> str, stringPool);
        regularName = new MappedString(names, 0, 1, (str) -> str.replace('/', '.'), stringPool);
    }

    public void addField(UniversalField field) {
        fields.unwrap(field.name, field);
    }

    public void addMethod(UniversalMethod method) {
        methods.unwrap(method.name, method);
    }

    public String getName(@NonNull NameType nameType, @NonNull MappingType mappingType) {
        switch (nameType) {
            case Internal:
                return internalName.get(mappingType);
            case Regular:
                return regularName.get(mappingType);
            default:
                throw new IllegalArgumentException("Invalid enum value " + nameType);
        }
    }
}
