/*
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.lib.internal.impl.dependencies;

import com.falsepattern.lib.dependencies.DependencyLoader;
import com.falsepattern.lib.dependencies.Library;
import com.falsepattern.lib.dependencies.Version;
import com.falsepattern.lib.internal.Internet;
import com.falsepattern.lib.internal.Share;
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.config.LibraryConfig;
import com.falsepattern.lib.util.FileUtil;
import io.netty.util.internal.ConcurrentSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class DependencyLoaderImpl {

    /**
     * Library checksum formats in a decreasing order of quality.
     */
    private static final String[] CHECKSUM_TYPES = new String[]{"sha512", "sha256", "sha1", "md5"};
    private static final Map<String, Version> loadedLibraries = new ConcurrentHashMap<>();
    private static final Map<String, String> loadedLibraryMods = new ConcurrentHashMap<>();
    private static final Set<String> mavenRepositories = new ConcurrentSet<>();
    private static final Logger log = LogManager.getLogger(Tags.MODNAME + " Library Downloader");

    private static final AtomicLong counter = new AtomicLong(0);
    private static final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        val thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("Dependency Download Thread " + counter.incrementAndGet());
        return thread;
    });

    public static void addMavenRepo(String url) {
        mavenRepositories.add(url);
    }

    @Deprecated
    public static void loadLibrary(@NonNull String loadingModId, @NonNull String groupId, @NonNull String artifactId, @NonNull Version minVersion, Version maxVersion, @NonNull Version preferredVersion, String regularSuffix, String devSuffix) {
        new DependencyLoadTask(loadingModId, groupId, artifactId, minVersion, maxVersion, preferredVersion,
                               regularSuffix, devSuffix).load();
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

    private static void checkedDelete(File file) {
        if (!file.delete()) {
            log.fatal("Failed to delete file {}", file);
            throw new RuntimeException("Failed to delete file " + file);
        }
    }

    private static synchronized void addToClasspath(File file) {
        try {
            val cl = (LaunchClassLoader) DependencyLoader.class.getClassLoader();
            cl.addURL(file.toURI().toURL());
            log.debug("Injected file {} into classpath!", file.getPath());
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

    public static void loadLibraries(Library... libraries) {
        loadLibrariesAsync(libraries).join();
    }

    public static CompletableFuture<Void> loadLibrariesAsync(Library... libraries) {
        val futures = new ArrayList<CompletableFuture<Void>>();
        for (val library : libraries) {
            val task = new DependencyLoadTask(library.loadingModId, library.groupId, library.artifactId,
                                              library.minVersion, library.maxVersion, library.preferredVersion,
                                              library.regularSuffix, library.devSuffix);
            futures.add(CompletableFuture.runAsync(task::load, executor));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @RequiredArgsConstructor
    private static class DependencyLoadTask {
        @NonNull
        private final String loadingModId;
        @NonNull
        private final String groupId;
        @NonNull
        private final String artifactId;
        @NonNull
        private final Version minVersion;
        private final Version maxVersion;
        @NonNull
        private final Version preferredVersion;
        private final String regularSuffix;
        private final String devSuffix;

        private String suffix;
        private String artifactLogName;
        private String artifact;
        private String mavenJarName;
        private String jarName;
        private File libDir;
        private File file;

        private void load() {
            setupLibraryNames();
            if (loadedLibraries.containsKey(artifact)) {
                alreadyLoaded();
                return;
            }
            setupPaths();
            if (tryLoadingExistingFile()) {
                return;
            }
            validateDownloadsAllowed();
            for (var repo : mavenRepositories) {
                if (tryDownloadFromMaven(repo)) {
                    return;
                }
            }
            crashCouldNotDownload();
        }

        private void crashCouldNotDownload() {
            val errorMessage = "Failed to download library " + groupId + ":" + artifactId + ":" + preferredVersion +
                               ((suffix != null) ? ":" + suffix : "") + " from any repository! Requested by mod: " +
                               loadingModId;
            log.fatal(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        private void setupLibraryNames() {
            suffix = Share.DEV_ENV ? devSuffix : regularSuffix;
            artifactLogName = String.format("%s:%s:%s%s", groupId, artifactId, preferredVersion,
                                            suffix != null ? "-" + suffix : "");
            log.info("Adding library {}, requested by mod {}", artifactLogName, loadingModId);
            artifact = groupId + ":" + artifactId + ":" + suffix;
        }

        private void alreadyLoaded() {
            val currentVer = loadedLibraries.get(artifact);
            if (currentVer.equals(preferredVersion)) {
                return;
            }
            val rangeString = "(minimum: " + minVersion + (maxVersion == null ? "" : ", maximum: " + maxVersion) + ")";
            if (minVersion.compareTo(currentVer) > 0 || (maxVersion != null && maxVersion.compareTo(currentVer) < 0)) {
                for (int i = 0; i < 16; i++) {
                    log.fatal("ALERT VVVVVVVVVVVV ALERT");
                }
                log.fatal("Library {}:{}{} already loaded with version {}, " +
                          "but a version in the range {} was requested! Thing may go horribly wrong! " +
                          "Requested by mod: {}, previously loaded by mod: {}", groupId, artifactId,
                          suffix != null ? ":" + suffix : "", currentVer, rangeString, loadingModId,
                          loadedLibraryMods.get(artifact));
                for (int i = 0; i < 16; i++) {
                    log.fatal("ALERT ^^^^^^^^^^^^ ALERT");
                }
            } else {
                log.info("Attempted loading of library {}:{}{} with preferred version {}, " +
                         "but version {} was already loaded, which matched the range {}. This is not an " + "error. " +
                         "Requested by mod: {}, previously loaded by mod: {}", groupId, artifactId,
                         suffix != null ? ":" + suffix : "", preferredVersion, currentVer, rangeString, loadingModId,
                         loadedLibraryMods.get(artifact));
            }
        }

        private void setupPaths() {
            var homeDir = System.getProperty("minecraft.sharedDataDir");
            if (homeDir == null) {
                homeDir = System.getenv("MINECRAFT_SHARED_DATA_DIR");
                if (homeDir == null) {
                    homeDir = FileUtil.getMinecraftHome().getAbsolutePath();
                }
            }
            val modsDir = Paths.get(homeDir, "mods").toFile();
            mavenJarName =
                    String.format("%s-%s%s.jar", artifactId, preferredVersion, (suffix != null) ? ("-" + suffix) : "");
            jarName = groupId + "-" + mavenJarName;
            libDir = new File(modsDir, "falsepattern");
            if (!libDir.exists()) {
                if (!libDir.mkdirs()) {
                    log.fatal("Failed to create directory {}", libDir);
                    throw new RuntimeException("Failed to create directory " + libDir);
                }
            }
            file = new File(libDir, jarName);
        }

        private boolean tryLoadingExistingFile() {
            if (!file.exists()) {
                return false;
            }
            try {
                val status = validateChecksum(file);
                if (status == ChecksumStatus.FAILED) {
                    return false;
                } else if (status == ChecksumStatus.MISSING) {
                    log.debug("Library {} is missing checksum data! Either it was manually deleted, " +
                              "or the source repo didn't have it in the first place", artifactLogName);
                }
            } catch (IOException e) {
                log.error("Failed to execute validation check for " + artifactLogName, e);
                checkedDelete(file);
                return false;
            }
            try {
                addToClasspath(file);
                loadedLibraries.put(artifact, preferredVersion);
                log.debug("Library {} successfully loaded from disk!", artifactLogName);
                return true;
            } catch (RuntimeException e) {
                log.warn("Failed to load library {} from file! Re-downloading...", artifactLogName);
                checkedDelete(file);
                return false;
            }
        }

        private void validateDownloadsAllowed() {
            if (!LibraryConfig.ENABLE_LIBRARY_DOWNLOADS) {
                val errorMessage = "Failed to load library " + groupId + ":" + artifactId + ":" + preferredVersion +
                                   ((suffix != null) ? ":" + suffix : "") + ": " + Tags.MODNAME +
                                   " library downloading has been disabled in the config, and the library is not present " +
                                   "on disk! Requested by mod: " + loadingModId;
                log.fatal(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        }

        private boolean tryDownloadFromMaven(String repo) {
            try {
                if (!repo.endsWith("/")) {
                    repo = repo + "/";
                }
                val url = String.format("%s%s/%s/%s/%s", repo, groupId.replace('.', '/'), artifactId, preferredVersion,
                                        mavenJarName);
                String finalRepo = repo;
                int retryCount = 0;
                while (true) {
                    retryCount++;
                    if (retryCount > 3) {
                        break;
                    }
                    val success = new AtomicBoolean(false);
                    Internet.connect(new URL(url),
                                     (ex) -> log.debug("Artifact {} could not be downloaded from repo {}: {}",
                                                       artifactLogName, finalRepo, ex.getMessage()), (input) -> {
                                log.debug("Downloading {} from {}", artifactLogName, finalRepo);
                                download(input, file);
                                log.debug("Downloaded {} from {}", artifactLogName, finalRepo);
                                success.set(true);
                            });
                    if (success.get()) {
                        log.debug("Validating checksum for {}", artifactLogName);
                        val hadChecksum = validateChecksum(url);
                        switch (hadChecksum) {
                            case FAILED:
                                continue;
                            case OK:
                                break;
                            case MISSING:
                                log.warn("The library {} had no checksum available on the repository.\n" +
                                         "There's a chance it might have gotten corrupted during download,\n" +
                                         "but we're loading it anyways.", artifactLogName);
                        }
                        loadedLibraries.put(artifact, preferredVersion);
                        loadedLibraryMods.put(artifact, loadingModId);
                        addToClasspath(file);
                        return true;
                    }
                }
            } catch (IOException ignored) {
            }
            return false;
        }

        private ChecksumStatus validateChecksum(String url) throws IOException {
            for (val checksumType : CHECKSUM_TYPES) {
                val checksumURL = url + "." + checksumType;
                val checksumFile = new File(libDir, jarName + "." + checksumType);
                log.debug("Attempting to get {} checksum...", checksumType);
                val success = new AtomicBoolean(false);
                Internet.connect(new URL(checksumURL),
                                 (ex) -> log.debug("Could not get {} checksum for {}: {}", checksumType,
                                                   artifactLogName, ex.getMessage()), (input) -> {
                            log.debug("Downloading {} checksum for {}", checksumType, artifactLogName);
                            download(input, checksumFile);
                            log.debug("Downloaded {} checksum for {}", checksumType, artifactLogName);
                            success.set(true);
                        });
                if (success.get()) {
                    return getChecksumStatus(file, checksumType, checksumFile);
                }
            }
            return ChecksumStatus.MISSING;
        }

        private ChecksumStatus validateChecksum(File file) throws IOException {
            for (val checksumType : CHECKSUM_TYPES) {
                val checksumFile = new File(libDir, jarName + "." + checksumType);
                log.debug("Attempting to read {} checksum from file...", checksumType);
                if (checksumFile.exists()) {
                    return getChecksumStatus(file, checksumType, checksumFile);
                }
            }
            return ChecksumStatus.MISSING;
        }

        private ChecksumStatus getChecksumStatus(File file, String checksumType, File checksumFile) throws IOException {
            val fileHash = hash(checksumType, file);
            val referenceHash = new String(Files.readAllBytes(checksumFile.toPath()));
            if (!fileHash.equals(referenceHash)) {
                log.error("Failed {} checksum validation for {}.", checksumType, artifactLogName);
                checkedDelete(file);
                checkedDelete(checksumFile);
                return ChecksumStatus.FAILED;
            }
            log.debug("Successfully validated {} checksum for {}.", checksumType, artifactLogName);
            return ChecksumStatus.OK;
        }

        private enum ChecksumStatus {
            OK,
            FAILED,
            MISSING
        }
    }
}
