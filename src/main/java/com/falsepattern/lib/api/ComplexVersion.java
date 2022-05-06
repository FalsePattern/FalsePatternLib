package com.falsepattern.lib.api;

import com.falsepattern.lib.dependencies.Version;
import lombok.NonNull;

/**
 * Deprecated in 0.6.*, will be removed in 0.7.0.
 * Moved to {@link com.falsepattern.lib.dependencies.ComplexVersion}.
 */
@Deprecated
public class ComplexVersion extends com.falsepattern.lib.dependencies.ComplexVersion {
    public ComplexVersion(@NonNull Version mainVersion, Version... subVersions) {
        super(mainVersion, subVersions);
        Deprecation.warn();
    }
}
