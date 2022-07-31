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

import com.falsepattern.lib.mapping.storage.MappedString;
import com.falsepattern.lib.util.ReflectionUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
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

    public UniversalField(@NonNull UniversalClass parent, String[] names, Map<String, String> stringPool) {
        this.parent = parent;
        name = new MappedString(names, 0, 1, (str) -> str.substring(str.lastIndexOf('/') + 1), stringPool);
        parent.addField(this);
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

    public FieldInsnNode asInstruction(int opcode, MappingType mapping, String descriptor) {
        return new FieldInsnNode(opcode, parent.getName(NameType.Internal, mapping), getName(mapping), descriptor);
    }
}
