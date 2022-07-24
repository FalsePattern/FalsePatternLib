package com.falsepattern.lib.dependencies;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.impl.dependencies.DependencyLoaderImpl;
import lombok.Builder;
import lombok.NonNull;


@StableAPI(since = "0.6.0")
public class DependencyLoader {

    public static void addMavenRepo(String url) {
        DependencyLoaderImpl.addMavenRepo(url);
    }

    @Builder
    public static void loadLibrary(@NonNull String loadingModId,
                                   @NonNull String groupId,
                                   @NonNull String artifactId,
                                   @NonNull Version minVersion,
                                   Version maxVersion,
                                   @NonNull Version preferredVersion,
                                   String regularSuffix,
                                   String devSuffix) {
        DependencyLoaderImpl.loadLibrary(loadingModId, groupId, artifactId, minVersion, maxVersion, preferredVersion,
                                         regularSuffix, devSuffix);
    }
}
