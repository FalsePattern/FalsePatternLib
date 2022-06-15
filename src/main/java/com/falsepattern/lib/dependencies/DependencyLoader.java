package com.falsepattern.lib.dependencies;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.falsepattern.lib.util.FileUtil;
import lombok.*;
import net.minecraft.launchwrapper.LaunchClassLoader;


@StableAPI(since = "0.6.0")
public class DependencyLoader {

    private static final Map<String, Version> loadedLibraries = new HashMap<>();
    private static final Map<String, String> loadedLibraryMods = new HashMap<>();
    private static final Set<String> mavenRepositories = new HashSet<>();

    public static void addMavenRepo(String url) {
        mavenRepositories.add(url);
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
        val suffix = FalsePatternLib.isDeveloperEnvironment() ? devSuffix : regularSuffix;
        val artifactLogName = String.format("%s:%s:%s%s", groupId, artifactId, preferredVersion, suffix != null ? "-" + suffix : "");
        FalsePatternLib.getLog()
                       .info("Adding library {}, requested by mod {}", artifactLogName,
                             loadingModId);
        var artifact = groupId + ":" + artifactId + ":" + suffix;
        if (loadedLibraries.containsKey(artifact)) {
            val currentVer = loadedLibraries.get(artifact);
            if (currentVer.equals(preferredVersion)) {
                return;
            }
            val rangeString = "(minimum: " + minVersion + (maxVersion == null ? "" : ", maximum: " + maxVersion) + ")";
            if (minVersion.compareTo(currentVer) > 0 || (maxVersion != null && maxVersion.compareTo(currentVer) < 0)) {
                for (int i = 0; i < 16; i++) {
                    FalsePatternLib.getLog().fatal("ALERT VVVVVVVVVVVV ALERT");
                }
                FalsePatternLib.getLog()
                               .fatal("Library {}:{}{} already loaded with version {}, " +
                                      "but a version in the range {} was requested! Thing may go horribly wrong! " +
                                      "Requested by mod: {}, previously loaded by mod: {}",
                                      groupId,
                                      artifactId,
                                      suffix != null ? ":" + suffix : "",
                                      currentVer,
                                      rangeString,
                                      loadingModId,
                                      loadedLibraryMods.get(artifact));
                for (int i = 0; i < 16; i++) {
                    FalsePatternLib.getLog().fatal("ALERT ^^^^^^^^^^^^ ALERT");
                }
            } else {
                FalsePatternLib.getLog()
                               .info("Attempted loading of library {}:{}{} with preferred version {}, " +
                                     "but version {} was already loaded, which matched the range {}. This is not an " +
                                     "error. " + "Requested by mod: {}, previously loaded by mod: {}",
                                     groupId,
                                     artifactId,
                                     suffix != null ? ":" + suffix : "",
                                     preferredVersion,
                                     currentVer,
                                     rangeString,
                                     loadingModId,
                                     loadedLibraryMods.get(artifact));
            }
            return;
        }
        val modsDir = new File(FileUtil.getMinecraftHome(), "mods");
        val mavenJarName =
                String.format("%s-%s%s.jar", artifactId, preferredVersion, (suffix != null) ? ("-" + suffix) : "");
        val jarName = groupId + "-" + mavenJarName;
        val libDir = new File(modsDir, "falsepattern");
        if (!libDir.exists()) {
            if (!libDir.mkdirs()) {
                FalsePatternLib.getLog().fatal("Failed to create directory {}", libDir);
                throw new RuntimeException("Failed to create directory " + libDir);
            }
        }
        val file = new File(libDir, jarName);
        if (file.exists()) {
            try {
                addToClasspath(file);
                loadedLibraries.put(artifact, preferredVersion);
                FalsePatternLib.getLog()
                               .info("Library {} successfully loaded from disk!", artifactLogName);
                return;
            } catch (RuntimeException e) {
                FalsePatternLib.getLog()
                               .warn("Failed to load library {} from file! Redownloading...", artifactLogName);
                if (!file.delete()) {
                    FalsePatternLib.getLog().fatal("Failed to delete file {}", file);
                    throw new RuntimeException("Failed to delete file " + file);
                }
            }
        }
        if (!LibraryConfig.ENABLE_LIBRARY_DOWNLOADS) {
            val errorMessage = "Failed to load library " + groupId + ":" + artifactId + ":" + preferredVersion +
                               ((suffix != null) ? ":" + suffix : "") + ": " + Tags.MODNAME +
                               " library downloading has been disabled in the config, and the library is not present " +
                               "on disk! Requested by mod: " + loadingModId;
            FalsePatternLib.getLog().fatal(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        for (var repo : mavenRepositories) {
            try {
                if (!repo.endsWith("/")) {
                    repo = repo + "/";
                }
                val url = new URL(String.format("%s%s/%s/%s/%s",
                                                repo,
                                                groupId.replace('.', '/'),
                                                artifactId,
                                                preferredVersion,
                                                mavenJarName));
                String finalRepo = repo;
                Internet.connect(url, (ex) -> {
                    FalsePatternLib.getLog()
                                   .info("Artifact {} could not be downloaded from repo {}: {}",
                                           artifactLogName,
                                           finalRepo,
                                           ex.getMessage());
                }, (input) -> {
                    FalsePatternLib.getLog()
                                   .info("Downloading {} from {}",
                                           artifactLogName,
                                           finalRepo);
                    download(input, file);
                    FalsePatternLib.getLog().info("Downloaded {}", artifactLogName);
                    loadedLibraries.put(artifact, preferredVersion);
                    loadedLibraryMods.put(artifact, loadingModId);
                    addToClasspath(file);
                });
                return;
            } catch (IOException ignored) {
            }
        }
        val errorMessage = "Failed to download library " + groupId + ":" + artifactId + ":" + preferredVersion +
                           ((suffix != null) ? ":" + suffix : "") + " from any repository! Requested by mod: " +
                           loadingModId;
        FalsePatternLib.getLog().fatal(errorMessage);
        throw new IllegalStateException(errorMessage);
    }

    private static void addToClasspath(File file) {
        try {
            val cl = (LaunchClassLoader) DependencyLoader.class.getClassLoader();
            cl.addURL(file.toURI().toURL());
            FalsePatternLib.getLog().info("Injected file {} into classpath!", file.getPath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to add library to classpath: " + file.getAbsolutePath(), e);
        }
    }

    @SneakyThrows
    private static void download(InputStream is, File target) {
        if (target.exists()) {
            return;
        }
        Internet.transferAndClose(is, new BufferedOutputStream(Files.newOutputStream(target.toPath())));
    }
}
