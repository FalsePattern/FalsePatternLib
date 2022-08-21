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
    static final Logger LOG = LogManager.getLogger(Tags.MODNAME + " ASM");

    @Getter
    private final List<IClassNodeTransformer> transformers;

    @Getter
    private final Logger logger = LOG;

    public FPTransformer() {
        transformers = Collections.singletonList(new IMixinPluginTransformer());
    }
}
