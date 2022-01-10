package com.falsepattern.lib.api;

import com.falsepattern.lib.FalsePatternLib;
import lombok.Builder;
import lombok.NonNull;

public class DependencyLoader {
    @Builder
    public static void loadLibrary(String loadingModId, String groupId, String artifactId, @NonNull Version minVersion, Version maxVersion, @NonNull Version preferredVersion, String devSuffix, boolean isMod) {
        FalsePatternLib.loadLibrary(loadingModId, groupId, artifactId, minVersion, maxVersion, preferredVersion, devSuffix, isMod);
    }

    public static void addMavenRepo(String url) {
        FalsePatternLib.addMavenRepo(url);
    }
}
