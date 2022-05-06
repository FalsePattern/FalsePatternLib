package com.falsepattern.lib.api;

/**
 * Deprecated in 0.6.*, will be removed in 0.7.0.
 * Moved to {@link com.falsepattern.lib.dependencies.SemanticVersion}.
 */
@Deprecated
public class SemanticVersion extends com.falsepattern.lib.dependencies.SemanticVersion {
    public SemanticVersion(int majorVersion, int minorVersion, int patchVersion, String preRelease, String build) {
        super(majorVersion, minorVersion, patchVersion, preRelease, build);
        Deprecation.warn();
    }

    public SemanticVersion(int majorVersion, int minorVersion, int patchVersion, String preRelease) {
        super(majorVersion, minorVersion, patchVersion, preRelease);
        Deprecation.warn();
    }

    public SemanticVersion(int majorVersion, int minorVersion, int patchVersion) {
        super(majorVersion, minorVersion, patchVersion);
        Deprecation.warn();
    }
}
