package com.falsepattern.lib.api;

import com.falsepattern.lib.FalsePatternLib;

public class DependencyLoader {
    public static void loadLibrary(String groupId, String artifactId, String version, String devSuffix, boolean isMod) {
        FalsePatternLib.loadLibrary(groupId, artifactId, version, devSuffix, isMod);
    }

    public static void addMavenRepo(String url) {
        FalsePatternLib.addMavenRepo(url);
    }
}
