package com.falsepattern.lib.api;

/**
 * Deprecated in 0.6.*, will be removed in 0.7.0.
 * Moved to {@link com.falsepattern.lib.dependencies.SemanticVersion}.
 */
@Deprecated
public abstract class Version extends com.falsepattern.lib.dependencies.Version {
    protected Version() {
        super();
        Deprecation.warn();
    }
}
