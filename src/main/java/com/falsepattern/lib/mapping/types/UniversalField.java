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
package com.falsepattern.lib.mapping.types;

import com.falsepattern.lib.internal.ReflectionUtil;
import com.falsepattern.lib.mapping.storage.MappedString;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;
import org.objectweb.asm.tree.FieldInsnNode;

import java.lang.reflect.Field;
import java.util.Map;

@Accessors(fluent = true)
@ToString
@EqualsAndHashCode
public class UniversalField {
    @Getter
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public final UniversalClass parent;

    @Getter
    public final MappedString name;

    private Field javaFieldCache = null;

    private UniversalField(@NonNull UniversalClass parent, String[] names, Map<String, String> stringPool) {
        this.parent = parent;
        name = new MappedString(names, 0, 1, (str) -> str.substring(str.lastIndexOf('/') + 1), stringPool);
        parent.addField(this);
    }

    public static void createAndAddToParent(@NonNull UniversalClass parent, String[] names, Map<String, String> stringPool) {
        new UniversalField(parent, names, stringPool);
    }

    public String getName(MappingType mappingType) {
        return name.get(mappingType);
    }

    public Field asJavaField() throws ClassNotFoundException, NoSuchFieldException {
        if (javaFieldCache != null) {
            return javaFieldCache;
        }
        val parentClass = parent.asJavaClass();
        javaFieldCache = parentClass.getDeclaredField(getName(parent.realClassMapping()));
        ReflectionUtil.jailBreak(javaFieldCache);
        return javaFieldCache;
    }

    /**
     * A convenience method for {@link Field#get(Object)} with a {@link SneakyThrows} annotation, so that
     * you don't need to manually handle exceptions.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Object instance) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        val field = asJavaField();
        return (T) field.get(instance);
    }

    public FieldInsnNode asInstruction(int opcode, MappingType mapping, String descriptor) {
        return new FieldInsnNode(opcode, parent.getName(NameType.Internal, mapping), getName(mapping), descriptor);
    }
}
