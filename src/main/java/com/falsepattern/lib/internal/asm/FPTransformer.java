package com.falsepattern.lib.internal.asm;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.asm.IClassNodeTransformer;
import com.falsepattern.lib.asm.SmartTransformer;
import com.falsepattern.lib.internal.Tags;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

@Accessors(fluent = true)
@StableAPI(since = "__INTERNAL__")
public class FPTransformer implements SmartTransformer {
    @Getter
    private final List<IClassNodeTransformer> transformers;

    @Getter
    private final Logger logger = LogManager.getLogger(Tags.MODNAME + " ASM");

    public FPTransformer() {
        boolean obsolete;
        try {
            Class.forName("io.github.tox1cozz.mixinbooterlegacy.MixinBooterLegacyPlugin");
            logger.info("Detected MixinBooter Legacy.");
            obsolete = false;
        } catch (ClassNotFoundException e) {
            logger.info("Detected SpongeMixins or Grimoire. Applying legacy compat fix to IMixinPlugin.");
            obsolete = true;
        }
        transformers = Collections.singletonList(new IMixinPluginTransformer(obsolete));
    }
}
