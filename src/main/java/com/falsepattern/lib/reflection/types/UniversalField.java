package com.falsepattern.lib.reflection.types;

import com.falsepattern.lib.reflection.ReflectionUtil;
import com.falsepattern.lib.reflection.storage.MappedString;
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
        parent.addField(this);
        name = new MappedString(names, 0, 1, (str) -> str.substring(str.lastIndexOf('/') + 1), stringPool);
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
