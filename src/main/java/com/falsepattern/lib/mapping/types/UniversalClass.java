/*
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.lib.mapping.types;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.asm.CoreLoadingPlugin;
import com.falsepattern.lib.mapping.storage.Lookup;
import com.falsepattern.lib.mapping.storage.MappedString;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;

@Accessors(fluent = true)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@StableAPI(since = "0.10.0")
public class UniversalClass {
    @Getter(onMethod_ = @StableAPI.Expose)
    @ToString.Include
    @EqualsAndHashCode.Include
    public final MappedString internalName;
    @Getter(onMethod_ = @StableAPI.Expose)
    @ToString.Include
    @EqualsAndHashCode.Include
    public final MappedString regularName;

    private final Lookup<UniversalField> fields = new Lookup<>();
    private final Lookup<UniversalMethod> methods = new Lookup<>();

    private Class<?> javaClassCache = null;
    @Getter
    private MappingType realClassMapping = null;

    @StableAPI.Expose
    public UniversalClass(String[] names, Map<String, String> stringPool) {
        internalName = new MappedString(names, 0, 1, (str) -> str, stringPool);
        regularName = new MappedString(names, 0, 1, (str) -> str.replace('/', '.'), stringPool);
    }

    void addField(UniversalField field) {
        if (field.parent != this) {
            throw new IllegalArgumentException("Field's parent is not this class");
        }
        fields.unwrap(field.name, field);
    }

    void addMethod(UniversalMethod method) {
        if (method.parent != this) {
            throw new IllegalArgumentException("Method's parent is not this class");
        }
        methods.unwrap(method.fusedNameDescriptor, method);
    }

    @StableAPI.Expose
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

    @StableAPI.Expose
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

    @StableAPI.Expose
    public String getNameAsDescriptor(MappingType mappingType) {
        return "L" + getName(NameType.Internal, mappingType) + ";";
    }

    @StableAPI.Expose
    public UniversalField getField(MappingType mappingType, String fieldName) throws NoSuchFieldException {
        try {
            return fields.get(mappingType, fieldName);
        } catch (Lookup.LookupException e) {
            throw new NoSuchFieldException(e.getMessage());
        }
    }

    @StableAPI.Expose
    public UniversalMethod getMethod(MappingType mappingType, String methodName, String methodDescriptor)
            throws NoSuchMethodException {
        try {
            return methods.get(mappingType, methodName + methodDescriptor);
        } catch (Lookup.LookupException e) {
            throw new NoSuchMethodException(e.getMessage());
        }
    }
}
