package com.falsepattern.lib.api;

import com.falsepattern.lib.version.Version;
import lombok.NonNull;

/**
 * Deprecated in 0.6.*, will be removed in 0.7.0.
 * Moved to {@link com.falsepattern.lib.version.ComplexVersion}.
 */
@Deprecated
public class ComplexVersion extends com.falsepattern.lib.version.ComplexVersion {
    public ComplexVersion(@NonNull Version mainVersion, Version... subVersions) {
        super(mainVersion, subVersions);
    }
}
