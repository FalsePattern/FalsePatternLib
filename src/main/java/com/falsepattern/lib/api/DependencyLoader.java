package com.falsepattern.lib.api;

import com.falsepattern.lib.FalsePatternLib;
import com.falsepattern.lib.version.Version;
import lombok.Builder;
import lombok.NonNull;

/**
 * Deprecated in 0.6.*, will be removed in 0.7.0.
 * Outdated wrapper on top of the {@link FalsePatternLib} class. Use that one instead.
 */
@Deprecated
public class DependencyLoader {
    @Builder
    public static void loadLibrary(String loadingModId, String groupId, String artifactId, @NonNull Version minVersion, Version maxVersion, @NonNull Version preferredVersion, String regularSuffix, String devSuffix) {
        FalsePatternLib.loadLibrary(loadingModId, groupId, artifactId, minVersion, maxVersion, preferredVersion, regularSuffix, devSuffix);
        Deprecation.warn();
    }

    public static void addMavenRepo(String url) {
        FalsePatternLib.addMavenRepo(url);
        Deprecation.warn();
    }
}
