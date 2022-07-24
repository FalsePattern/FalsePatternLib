package com.falsepattern.lib.dependencies;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.falsepattern.lib.util.FileUtil;
import lombok.*;
import net.minecraft.launchwrapper.LaunchClassLoader;


@StableAPI(since = "0.6.0")
public class DependencyLoader {

    /**
     * Dependency checksum formats in a decreasing order of quality.
     */
    private static final String[] CHECKSUM_TYPES = new String[]{"sha512", "sha256", "sha1", "md5"};

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
        String homeDir = System.getProperty("minecraft.sharedDataDir");
        if (homeDir == null) {
            homeDir = System.getenv("MINECRAFT_SHARED_DATA_DIR");
            if (homeDir == null) {
                homeDir = FileUtil.getMinecraftHome().getAbsolutePath();
            }
        }
        val modsDir = Paths.get(homeDir, "mods").toFile();
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
                    val url = String.format("%s%s/%s/%s/%s", repo, groupId.replace('.', '/'), artifactId,
                                            preferredVersion, mavenJarName);
                    String finalRepo = repo;
                    boolean retry = true;
                    int retryCount = 0;
                    redownload:
                    while (retry) {
                        retry = false;
                        retryCount++;
                        if (retryCount >= 3) {
                            break;
                        }
                        val success = new AtomicBoolean(false);
                        Internet.connect(new URL(url), (ex) -> {
                            FalsePatternLib.getLog()
                                           .info("Artifact {} could not be downloaded from repo {}: {}", artifactLogName,
                                                 finalRepo, ex.getMessage());
                        }, (input) -> {
                            FalsePatternLib.getLog().info("Downloading {} from {}", artifactLogName, finalRepo);
                            download(input, file);
                            FalsePatternLib.getLog().info("Downloaded {}", artifactLogName);
                            success.set(true);
                        });
                        if (success.get()) {
                            FalsePatternLib.getLog().info("Validating checksum for {}", artifactLogName);
                            boolean hadChecksum = false;
                            for (val checksumType : CHECKSUM_TYPES) {
                                val checksumURL = url + "." + checksumType;
                                val checksumFile = new File(libDir, jarName + "." + checksumType);
                                FalsePatternLib.getLog().info("Attempting to get {} checksum...", checksumType);
                                success.set(false);
                                Internet.connect(new URL(checksumURL), (ex) -> {
                                    FalsePatternLib.getLog()
                                                   .info("Could not get {} checksum for {}: {}", checksumType,
                                                         artifactLogName, ex.getMessage());
                                }, (input) -> {
                                    FalsePatternLib.getLog()
                                                   .info("Downloading {} checksum for {}", checksumType, artifactLogName);
                                    download(input, checksumFile);
                                    FalsePatternLib.getLog()
                                                   .info("Downloaded {} checksum for {}", checksumType, artifactLogName);
                                    success.set(true);
                                });
                                if (success.get()) {
                                    val fileHash = hash(checksumType, file);
                                    val referenceHash = new String(Files.readAllBytes(checksumFile.toPath()));
                                    if (!fileHash.equals(referenceHash)) {
                                        FalsePatternLib.getLog()
                                                       .error("Failed {} checksum validation for {}. Retrying download...",
                                                              checksumType, artifactLogName);
                                        file.delete();
                                        retry = true;
                                        continue redownload;
                                    }
                                    FalsePatternLib.getLog()
                                                   .info("Successfully validated {} checksum for {}", checksumType,
                                                         artifactLogName);
                                    hadChecksum = true;
                                }
                            }
                            if (!hadChecksum) {
                                FalsePatternLib.getLog()
                                               .warn("The library {} had no checksum available on the repository.\n" +
                                                     "There's a chance it might have gotten corrupted during download,\n" +
                                                     "but we're loading it anyways.", artifactLogName);
                            }
                            loadedLibraries.put(artifact, preferredVersion);
                            loadedLibraryMods.put(artifact, loadingModId);
                            addToClasspath(file);
                            return;
                        }
                    }
                } catch (IOException ignored) {
                }
        }
        val errorMessage = "Failed to download library " + groupId + ":" + artifactId + ":" + preferredVersion + ((suffix != null) ? ":" + suffix : "") + " from any repository! Requested by mod: " +
                           loadingModId;
        FalsePatternLib.getLog().fatal(errorMessage);
        throw new IllegalStateException(errorMessage);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @SneakyThrows
    private static String digest(String algo, byte[] data) {
        return bytesToHex(MessageDigest.getInstance(algo).digest(data));
    }

    @SneakyThrows
    private static String hash(String algo, File file) {
        byte[] data = Files.readAllBytes(file.toPath());
        switch (algo) {
            case "md5":
                algo = "MD5";
                break;
            case "sha1":
                algo = "SHA-1";
                break;
            case "sha256":
                algo = "SHA-256";
                break;
            case "sha512":
                algo = "SHA-512";
                break;
        }
        return digest(algo, data);
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
