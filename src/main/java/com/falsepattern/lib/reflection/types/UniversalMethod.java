package com.falsepattern.lib.reflection.types;

import com.falsepattern.lib.reflection.ReflectionUtil;
import com.falsepattern.lib.reflection.storage.MappedString;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;
import org.objectweb.asm.tree.MethodInsnNode;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

@Accessors(fluent = true)
@ToString
@EqualsAndHashCode
public class UniversalMethod {
    @Getter
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    public final UniversalClass parent;

    @Getter
    public final MappedString name;
    @Getter
    public final MappedString descriptor;
    @Getter
    public final MappedString fusedNameDescriptor;

    private Method javaMethodCache = null;

    public UniversalMethod(@NonNull UniversalClass parent, String[] names, Map<String, String> stringPool) {
        this.parent = parent;
        parent.addMethod(this);
        name = new MappedString(names, 0, 2, (str) -> str.substring(str.lastIndexOf('/') + 1), stringPool);
        descriptor = new MappedString(names, 1, 2, (str) -> str, stringPool);
        fusedNameDescriptor = MappedString.fuse(name, descriptor, "", stringPool);
    }

    public String getName(MappingType mappingType) {
        return name.get(mappingType);
    }

    public String getDescriptor(MappingType mappingType) {
        return descriptor.get(mappingType);
    }

    public Method asJavaMethod() throws ClassNotFoundException, NoSuchMethodException {
        if (javaMethodCache != null) {
            return javaMethodCache;
        }
        val parentClass = parent.asJavaClass();
        javaMethodCache = parentClass.getDeclaredMethod(getName(parent.realClassMapping()), decodeMethodDescriptor(getDescriptor(parent.realClassMapping())));
        ReflectionUtil.jailBreak(javaMethodCache);
        return javaMethodCache;
    }

    private static Class<?>[] decodeMethodDescriptor(String desc) throws ClassNotFoundException {
        val result = new ArrayList<Class<?>>();
        val buf = new StringBuilder();
        boolean readingReference = false;
        int arrayDimensions = 0;
        for (val c: desc.toCharArray()) {
            Class<?> parsedClass = null;
            if (readingReference) {
                if (c == ';') {
                    parsedClass = Class.forName(buf.toString().replace('/', '.'));
                    buf.setLength(0);
                    readingReference = false;
                } else {
                    buf.append(c);
                    continue;
                }
            } else if (c == '(') continue;
            else if (c == ')') break;
            else switch (c) {
                    case 'B':
                        parsedClass = byte.class;
                        break;
                    case 'C':
                        parsedClass = char.class;
                        break;
                    case 'D':
                        parsedClass = double.class;
                        break;
                    case 'F':
                        parsedClass = float.class;
                        break;
                    case 'I':
                        parsedClass = int.class;
                        break;
                    case 'J':
                        parsedClass = long.class;
                        break;
                    case 'L':
                        readingReference = true;
                        continue;
                    case 'S':
                        parsedClass = short.class;
                        break;
                    case 'Z':
                        parsedClass = boolean.class;
                        break;
                    case '[':
                        arrayDimensions++;
                        continue;
            }
            for (int i = 0; i < arrayDimensions; i++) {
                parsedClass = Array.newInstance(parsedClass, 0).getClass();
            }
            arrayDimensions = 0;
            result.add(parsedClass);
        }
        return result.toArray(new Class[0]);
    }

    /**
     * This is only here for completeness' sake, given that MethodInsnNode itself also has a deprecated yet functional constructor.
     */
    @Deprecated
    public MethodInsnNode asInstruction(int opcode, MappingType mapping) {
        return new MethodInsnNode(opcode, parent.getName(NameType.Internal, mapping), getName(mapping), getDescriptor(mapping));
    }

    public MethodInsnNode asInstruction(int opcode, MappingType mapping, boolean itf) {
        return new MethodInsnNode(opcode, parent.getName(NameType.Internal, mapping), getName(mapping), getDescriptor(mapping), itf);
    }
}
