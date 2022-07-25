package com.falsepattern.lib.asm;

import com.falsepattern.lib.asm.exceptions.AsmTransformException;
import com.falsepattern.lib.internal.CoreLoadingPlugin;
import lombok.val;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import net.minecraft.launchwrapper.IClassTransformer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * An ASM transformation dispatcher utility, inspired by mixins.
 */
public interface SmartTransformer extends IClassTransformer {
    Logger logger();
    List<IClassNodeTransformer> transformers();
    @Override
    default byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        val transformers = new ArrayList<IClassNodeTransformer>();
        val cn = ASMUtil.parseClass(bytes, ClassReader.EXPAND_FRAMES);
        for (val transformer: transformers()) {
            if (transformer.shouldTransform(cn, transformedName, CoreLoadingPlugin.isObfuscated())) {
                transformers.add(transformer);
            }
        }
        if (transformers.isEmpty()) {
            return bytes;
        }
        transformers.sort(Comparator.comparingInt(IClassNodeTransformer::internalSortingOrder));
        val log = logger();
        for (val transformer: transformers) {
            log.debug("Patching {} with {}...", transformedName, transformer.getName());
            try {
                transformer.transform(cn, transformedName, CoreLoadingPlugin.isObfuscated());
            } catch (RuntimeException | Error t) {
                log.error("Error transforming {} with {}: {}", transformedName, transformer.getName(), t.getMessage());
                throw t;
            } catch (Throwable t) {
                log.error("Error transforming {} with {}: {}", transformedName, transformer.getName(), t.getMessage());
                throw new RuntimeException(t);
            }
        }
        val result = ASMUtil.serializeClass(cn, ClassWriter.COMPUTE_FRAMES);
        log.debug("Patched {} successfully.", transformedName);
        return result;
    }
}
