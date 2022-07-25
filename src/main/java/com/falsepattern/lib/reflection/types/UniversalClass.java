package com.falsepattern.lib.reflection.types;

import com.falsepattern.lib.internal.CoreLoadingPlugin;
import com.falsepattern.lib.reflection.storage.Lookup;
import com.falsepattern.lib.reflection.storage.MappedString;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;

@Accessors(fluent = true)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UniversalClass {
    @Getter
    @ToString.Include
    @EqualsAndHashCode.Include
    public final MappedString internalName;
    @Getter
    @ToString.Include
    @EqualsAndHashCode.Include
    public final MappedString regularName;

    private final Lookup<UniversalField> fields = new Lookup<>();
    private final Lookup<UniversalMethod> methods = new Lookup<>();

    private Class<?> javaClassCache = null;
    @Getter
    private MappingType realClassMapping = null;

    public UniversalClass(String[] names, Map<String, String> stringPool) {
        internalName = new MappedString(names, 0, 1, (str) -> str, stringPool);
        regularName = new MappedString(names, 0, 1, (str) -> str.replace('/', '.'), stringPool);
    }

    public void addField(UniversalField field) {
        if (field.parent != this) {
            throw new IllegalArgumentException("Field's parent is not this class");
        }
        fields.unwrap(field.name, field);
    }

    public void addMethod(UniversalMethod method) {
        if (method.parent != this) {
            throw new IllegalArgumentException("Method's parent is not this class");
        }
        methods.unwrap(method.fusedNameDescriptor, method);
    }

    public Class<?> asJavaClass() throws ClassNotFoundException {
        if (javaClassCache != null) {
            return javaClassCache;
        }
        if (!CoreLoadingPlugin.isObfuscated()) {
            javaClassCache = Class.forName(regularName.mcp);
            realClassMapping = MappingType.MCP;
        } else {
            try {
                javaClassCache = Class.forName(regularName.srg);
                realClassMapping = MappingType.SRG;
            } catch (ClassNotFoundException e) {
                //This should never happen, but we attempt recovery anyway
                try {
                    javaClassCache = Class.forName(regularName.notch);
                    realClassMapping = MappingType.Notch;
                } catch (ClassNotFoundException ex) {
                    throw e;
                }
            }
        }
        return javaClassCache;
    }

    public String getName(NameType nameType, MappingType mappingType) {
        switch (nameType) {
            case Internal:
                return internalName.get(mappingType);
            case Regular:
                return regularName.get(mappingType);
            default:
                throw new IllegalArgumentException("Invalid enum value " + nameType);
        }
    }

    public UniversalField getField(MappingType mappingType, String fieldName) throws NoSuchFieldException {
        try {
            return fields.get(mappingType, fieldName);
        } catch (Lookup.LookupException e) {
            throw new NoSuchFieldException(e.getMessage());
        }
    }

    public UniversalMethod getMethod(MappingType mappingType, String methodName, String methodDescriptor) throws NoSuchMethodException {
        try {
            return methods.get(mappingType, methodName + methodDescriptor);
        } catch (Lookup.LookupException e) {
            throw new NoSuchMethodException(e.getMessage());
        }
    }
}
