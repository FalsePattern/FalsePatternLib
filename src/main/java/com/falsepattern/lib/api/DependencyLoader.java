package com.falsepattern.lib.api;

import com.falsepattern.lib.dependencies.Version;
import lombok.Builder;
import lombok.NonNull;

/**
 * Deprecated in 0.6.*, will be removed in 0.7.0.
 * Outdated wrapper on top of the {@link com.falsepattern.lib.dependencies.DependencyLoader} class. Use that one instead.
 */
@Deprecated
public class DependencyLoader {
    @Builder
    public static void loadLibrary(String loadingModId, String groupId, String artifactId, @NonNull com.falsepattern.lib.dependencies.Version minVersion, com.falsepattern.lib.dependencies.Version maxVersion, @NonNull com.falsepattern.lib.dependencies.Version preferredVersion, String regularSuffix, String devSuffix) {
        com.falsepattern.lib.dependencies.DependencyLoader.loadLibrary(loadingModId, groupId, artifactId, minVersion, maxVersion, preferredVersion, regularSuffix, devSuffix);
        Deprecation.warn();
    }

    public static void addMavenRepo(String url) {
        com.falsepattern.lib.dependencies.DependencyLoader.addMavenRepo(url);
        Deprecation.warn();
    }
}
