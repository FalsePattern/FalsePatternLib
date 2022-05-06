package com.falsepattern.lib.dependencies;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.CoreLoadingPlugin;
import com.falsepattern.lib.internal.FalsePatternLib;
import lombok.NonNull;
import lombok.val;
import lombok.var;
import net.minecraft.launchwrapper.LaunchClassLoader;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.*;


@StableAPI(since = "0.6.0")
public class DependencyLoader {

    private static final Map<String, Version> loadedLibraries = new HashMap<>();
    private static final Map<String, String> loadedLibraryMods = new HashMap<>();
    private static final Set<String> mavenRepositories = new HashSet<>();

    public static void addMavenRepo(String url) {
        mavenRepositories.add(url);
    }

    public static void loadLibrary(String loadingModId, String groupId, String artifactId, @NonNull Version minVersion, Version maxVersion, @NonNull Version preferredVersion, String regularSuffix, String devSuffix) {
        val suffix = FalsePatternLib.isDeveloperEnvironment() ? devSuffix : regularSuffix;
        FalsePatternLib.getLog().info("Adding library {}:{}:{}{}, requested by mod {}", groupId, artifactId, preferredVersion, suffix != null ? "-" + suffix : "", loadingModId);
        var artifact = groupId + ":" + artifactId + ":" + suffix;
        if (loadedLibraries.containsKey(artifact)) {
            val currentVer = loadedLibraries.get(artifact);
            if (currentVer.equals(preferredVersion)) return;
            val rangeString = "(minimum: " + minVersion + (maxVersion == null ? "" : ", maximum: " + maxVersion) + ")";
            if (minVersion.compareTo(currentVer) > 0 || (maxVersion != null && maxVersion.compareTo(currentVer) < 0)) {
                for (int i = 0; i < 16; i++) {
                    FalsePatternLib.getLog().fatal("ALERT VVVVVVVVVVVV ALERT");
                }
                FalsePatternLib.getLog().fatal("Library {}:{}{} already loaded with version {}, " +
                                               "but a version in the range {} was requested! Thing may go horribly wrong! " +
                                               "Requested by mod: {}, previously loaded by mod: {}",
                        groupId, artifactId, suffix != null ? ":" + suffix : "", currentVer,
                        rangeString,
                        loadingModId, loadedLibraryMods.get(artifact));
                for (int i = 0; i < 16; i++) {
                    FalsePatternLib.getLog().fatal("ALERT ^^^^^^^^^^^^ ALERT");
                }
            } else {
                FalsePatternLib.getLog().info("Attempted loading of library {}:{}{} with preferred version {}, " +
                                              "but version {} was already loaded, which matched the range {}. This is not an error. " +
                                              "Requested by mod: {}, previously loaded by mod: {}",
                        groupId, artifactId, suffix != null ? ":" + suffix : "", preferredVersion,
                        currentVer, rangeString,
                        loadingModId, loadedLibraryMods.get(artifact));
            }
            return;
        }
        val modsDir = new File(CoreLoadingPlugin.mcDir, "mods");
        val mavenJarName = String.format("%s-%s%s.jar", artifactId, preferredVersion, (suffix != null) ? ("-" + suffix) : "");
        val jarName = groupId + "-" + mavenJarName;
        val libDir = new File(modsDir, "falsepattern");
        if (!libDir.exists()) {
            libDir.mkdirs();
        }
        val file = new File(libDir, jarName);
        if (file.exists()) {
            try {
                addToClasspath(file);
                loadedLibraries.put(artifact, preferredVersion);
                FalsePatternLib.getLog().info("Library {}:{}:{}{} successfully loaded from disk!", groupId, artifactId, preferredVersion, (suffix != null) ? ":" + suffix : "");
                return;
            } catch (RuntimeException e) {
                FalsePatternLib.getLog().warn("Failed to load library {}:{}:{}{} from file! Redownloading...", groupId, artifactId, preferredVersion, (suffix != null) ? ":" + suffix : "");
                file.delete();
            }
        }
        for (var repo: mavenRepositories) {
            try {
                if (!repo.endsWith("/")) repo = repo + "/";
                val url = new URL(String.format("%s%s/%s/%s/%s", repo, groupId.replace('.', '/'), artifactId, preferredVersion, mavenJarName));

                val connection = (HttpsURLConnection) url.openConnection();
                connection.setConnectTimeout(1500);
                connection.setReadTimeout(1500);
                connection.setRequestProperty("User-Agent", "FalsePatternLib Downloader");
                if (connection.getResponseCode() != 200) {
                    FalsePatternLib.getLog().info("Artifact {}:{}:{}{} was not found on repo {}", groupId, artifactId, preferredVersion, (suffix != null) ? ":" + suffix : "", repo);
                    connection.disconnect();
                    continue;
                }
                FalsePatternLib.getLog().info("Downloading {}:{}:{}{} from {}", groupId, artifactId, preferredVersion, (suffix != null) ? ":" + suffix : "", repo);
                download(connection.getInputStream(), file);
                FalsePatternLib.getLog().info("Downloaded {}:{}:{}{}", groupId, artifactId, preferredVersion, (suffix != null) ? ":" + suffix : "");
                loadedLibraries.put(artifact, preferredVersion);
                loadedLibraryMods.put(artifact, loadingModId);
                addToClasspath(file);
                return;
            } catch (IOException ignored) {}
        }
        val errorMessage = "Failed to download library " + groupId + ":" + artifactId + ":" + preferredVersion + ((suffix != null) ? ":" + suffix : "") + " from any repository! Requested by mod: " + loadingModId;
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

    private static void download(InputStream is, File target) throws IOException {
        if (target.exists()) return;

        var bytesRead = 0;

        val fileOutput = new BufferedOutputStream(new FileOutputStream(target));
        byte[] smallBuffer = new byte[4096];
        while ((bytesRead = is.read(smallBuffer)) >= 0) {
            fileOutput.write(smallBuffer, 0, bytesRead);
        }
        fileOutput.close();
        is.close();
    }
}
