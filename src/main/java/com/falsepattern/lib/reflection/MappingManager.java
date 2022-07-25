package com.falsepattern.lib.reflection;

import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.reflection.storage.Lookup;
import com.falsepattern.lib.reflection.types.MappingType;
import com.falsepattern.lib.reflection.types.NameType;
import com.falsepattern.lib.util.ResourceUtil;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

import java.io.IOException;
import java.util.HashMap;

public class MappingManager {
    private static boolean initialized = false;

    private static final Lookup<UniversalClass> internalLookup = new Lookup<>();
    private static final Lookup<UniversalClass> regularLookup = new Lookup<>();


    @SneakyThrows
    private static synchronized void initialize() {
        if (initialized) return;
        initialized = true;
        val stringPool = new HashMap<String, String>();
        {
            val classMappings = ResourceUtil.getResourceStringFromJar("/classes.csv", FalsePatternLib.class).split("\n");
            for (int i = 1; i < classMappings.length; i++) {
                val line = classMappings[i].split(",");
                val clazz = new UniversalClass(line, stringPool);
                internalLookup.unwrap(clazz.internalName, clazz);
                regularLookup.unwrap(clazz.regularName, clazz);
            }
        }
        {
            val fieldMappings = ResourceUtil.getResourceStringFromJar("/fields.csv", FalsePatternLib.class).split("\n");
            for (int i = 1; i < fieldMappings.length; i++) {
                val line = fieldMappings[i].split(",");
                val field = new UniversalField(line, stringPool);
                val clazz = internalLookup.get(MappingType.Notch, line[0].substring(0, line[0].lastIndexOf('/')));
                clazz.addField(field);
            }
        }
        {
            val methodMappings = ResourceUtil.getResourceStringFromJar("/methods.csv", FalsePatternLib.class).split("\n");
            for (int i = 1; i < methodMappings.length; i++) {
                val line = methodMappings[i].split(",");
                val field = new UniversalMethod(line, stringPool);
                val clazz = internalLookup.get(MappingType.Notch, line[0].substring(0, line[0].lastIndexOf('/')));
                clazz.addMethod(field);
            }
        }
    }

    public static UniversalClass classForName(@NonNull NameType nameType, @NonNull MappingType mappingType, String className) {
        initialize();
        switch (nameType) {
            case Internal:
                return internalLookup.get(mappingType, className);
            case Regular:
                return regularLookup.get(mappingType, className);
            default:
                throw new IllegalArgumentException("Invalid enum value " + nameType);
        }
    }
}
