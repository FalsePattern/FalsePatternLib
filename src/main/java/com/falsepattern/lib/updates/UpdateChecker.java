package com.falsepattern.lib.updates;

import com.falsepattern.lib.dependencies.DependencyLoader;
import com.falsepattern.lib.dependencies.SemanticVersion;
import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.internal.LibraryConfig;
import com.falsepattern.lib.internal.Tags;
import lombok.val;
import lombok.var;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpdateChecker {
    private static AtomicBoolean jsonLibraryLoaded = new AtomicBoolean(false);
    private static final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor((runnable) -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName(Tags.MODNAME + " Asynchronous Update Check Thread");
        return thread;
    });
    /**
     * Same this as {@link #fetchUpdates(String)}, but defers the check to a different thread. Useful for asynchronous
     * update checks, if you don't want to block loading.
     * @param url The URL to check
     * @return A future that will contain the update info about mods that were both available on the URL and installed
     */
    public static Future<List<ModUpdateInfo>> fetchUpdatesAsync(String url) {
        return asyncExecutor.submit(() -> fetchUpdates(url));
    }

    /**
     * Checks for updates. The URL should be a JSON file that contains a list of mods, each with a mod ID, one or more
     * versions, and a URL for the user to check for updates in case the current and latest versions are different.
     * The JSON file must have the following format:
     * <pre>{@code
     *  [
     *      {
     *          "modID": "modid",
     *          "latestVersion": ["1.0.0", "1.0.0-foo"],
     *          "updateURL": "https://example.com/mods/mymod"
     *      },
     *      {
     *          "modID": "modid2",
     *          "latestVersion": ["0.2.0", "0.3.0-alpha"],
     *          "updateURL": "https://example.com/mods/mymod2"
     *      },
     *      ...etc, one json object per mod.
     *  ]
     * }</pre>
     * @param url The URL to check
     * @return A list of mods that were both available on the URL and installed
     */
    public static List<ModUpdateInfo> fetchUpdates(String url) {
        if (!LibraryConfig.ENABLE_UPDATE_CHECKER) {
            return null;
        }
        if (!jsonLibraryLoaded.get()) {
            DependencyLoader.addMavenRepo("https://maven.falsepattern.com/");
            try {
                DependencyLoader.builder()
                                .groupId("com.falsepattern")
                                .artifactId("json")
                        .minVersion(new SemanticVersion(0, 4, 0))
                        .maxVersion(new SemanticVersion(0, Integer.MAX_VALUE, Integer.MAX_VALUE))
                        .build();
            } catch (Exception e) {
                FalsePatternLib.getLog().error("Failed to load json library for update checker!", e);
                return null;
            }
            jsonLibraryLoaded.set(true);
        }
        //TODO
        return null;
    }
}
