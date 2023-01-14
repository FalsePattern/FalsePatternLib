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

import com.falsepattern.lib.DeprecationDetails;
import com.falsepattern.lib.dependencies.Library;
import com.falsepattern.lib.dependencies.RawVersion;
import com.falsepattern.lib.dependencies.SemanticVersion;
import com.falsepattern.lib.dependencies.Version;
import com.falsepattern.lib.internal.Internet;
import com.falsepattern.lib.internal.Share;
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.config.LibraryConfig;
import com.falsepattern.lib.util.FileUtil;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import io.netty.util.internal.ConcurrentSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public class DependencyLoaderImpl {

    /**
     * Library checksum formats in a decreasing order of quality.
     */
    private static final String[] CHECKSUM_TYPES = new String[]{"sha512", "sha256", "sha1", "md5"};
    private static final Map<String, Version> loadedLibraries = new ConcurrentHashMap<>();
    private static final Map<String, String> loadedLibraryMods = new ConcurrentHashMap<>();
    private static final Set<String> mavenRepositories = new ConcurrentSet<>();
    private static final Logger LOG = LogManager.getLogger(Tags.MODNAME + " Library Loader");

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
            LOG.fatal("Failed to delete file {}", file);
            throw new RuntimeException("Failed to delete file " + file);
        }
    }

    private static synchronized void addToClasspath(File file) {
        try {
            val cl = (LaunchClassLoader) DependencyLoaderImpl.class.getClassLoader();
            cl.addURL(file.toURI().toURL());
            LOG.debug("Injected file {} into classpath!", file.getPath());
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

    @Deprecated
    @DeprecationDetails(deprecatedSince = "0.11.0")
    public static void loadLibraries(Library... libraries) {
        loadLibrariesAsync(libraries).join();
    }

    @Deprecated
    @DeprecationDetails(deprecatedSince = "0.11.0")
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

    private static Stream<URL> scanSourceMetaInf(URL source) {
        if (!source.getProtocol().equals("file")) {
            LOG.warn("Skipping non-file source: {}", source);
            return Stream.empty();
        }
        LOG.debug("Scanning {} for dependencies", source);
        val path = source.getPath();
        val output = new ArrayList<URL>();
        if (path.endsWith(".jar") || path.endsWith(".zip")) {
            //Scan jar file for json in META-INF, add them to the list
            try (val jarFile = (ZipFile)(path.endsWith("jar") ? new JarFile(path) : new ZipFile(path))) {
                val entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement();
                    if (!entry.getName().startsWith("META-INF") || !entry.getName().endsWith(".json")) {
                        continue;
                    }
                    try {
                        output.add(new URL("jar:" + source + "!/" + entry.getName()));
                    } catch (MalformedURLException e) {
                        LOG.error("Failed to add json source {} to dependency source list: {}", entry.getName(), e);
                    }
                }
            } catch (IOException e) {
                LOG.error("Failed to open jar file {}", path);
            }
        } else {
            val dir = new File(path);
            if (!dir.exists() || !dir.isDirectory()) {
                LOG.warn("Skipping non-directory, nor jar, nor zip source: {}", source);
                return Stream.empty();
            }
            //Scan directory for json in META-INF, add them to the list
            val metaInf = new File(dir, "META-INF");
            if (!metaInf.exists() || !metaInf.isDirectory()) {
                return Stream.empty();
            }
            val files = metaInf.listFiles();
            if (files == null) {
                return Stream.empty();
            }
            for (val file : files) {
                if (!file.getName().endsWith(".json")) {
                    continue;
                }
                try {
                    output.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    LOG.error("Failed to add json source {} to dependency source list: {}", file.getName(), e);
                }
            }
        }
        return output.stream();
    }

    private static Stream<URL> grabSourceCandidatesFromFolder(File folder) {
        if (!folder.exists() || !folder.isDirectory()) {
            LOG.warn("File does not exist or is not a directory: {}", folder);
        } else {
            val files = folder.listFiles();
            if (files == null) {
                LOG.warn("Folder is not readable: {}", folder);
            } else {
                return Arrays.stream(files).map(file -> {
                    try {
                        LOG.trace("Adding {} to dependency source list", file);
                        return file.toURI().toURL();
                    } catch (MalformedURLException e) {
                        LOG.error("Failed to add mod file {} to dependency source list: {}", file.getName(), e);
                        return null;
                    }
                }).filter(Objects::nonNull);
            }
        }
        return Stream.empty();
    }

    private static <A, B, C> Stream<Pair<A, B>> flatMap(Pair<A, C> pair, Function<C, Stream<B>> mapper) {
        val a = pair.a;
        return mapper.apply(pair.b).map(b -> new Pair<>(a, b));
    }

    private static final Pattern VERSION_PATTERN = Pattern.compile(""
                                                                   + "(0|[1-9]\\d*)(?:\\.(0|[1-9]\\d*))?(?:\\.(0|[1-9]\\d*))?"
                                                                   + "(?:-((?:(?:[0-9]+[a-zA-Z-][\\w-]*)|(?:[a-zA-Z][\\w-]*)|(?:[1-9]\\d*)|0)"
                                                                   + "(?:\\.(?:(?:[0-9]+[a-zA-Z-][\\w-]*)|(?:[a-zA-Z][\\w-]*)|(?:[1-9]\\d*)|0))*))?"
                                                                   + "(?:\\+([\\w-]+(\\.[\\w-]+)*))?");
    public static void scanDeps() {
        LOG.debug("Discovering dependency source candidates...");
        val modsDir = new File(FileUtil.getMinecraftHome(), "mods");
        val mods1710Dir = new File(modsDir, "1.7.10");
        val dependencySpecs = Stream.of(Launch.classLoader.getSources().stream(),
                                        grabSourceCandidatesFromFolder(modsDir),
                                        grabSourceCandidatesFromFolder(mods1710Dir))
                                    .flatMap((i) -> i)
                                    .flatMap(DependencyLoaderImpl::scanSourceMetaInf)
                                    .map((source) -> {
                                        //Convert source to GSON json
                                        try (val is = source.openStream()) {
                                            val json = new JsonParser().parse(new InputStreamReader(is)).getAsJsonObject();
                                            if (!(json.isJsonObject() &&
                                                  json.has("identifier") &&
                                                  json.get("identifier")
                                                      .getAsString()
                                                      .equals("falsepatternlib_dependencies")
                                            )) {
                                                return null;
                                            }
                                            val builder = new GsonBuilder();
                                            builder.excludeFieldsWithoutExposeAnnotation();
                                            val gson = builder.create();
                                            json.remove("identifier");
                                            val root = gson.fromJson(json, DepRoot.class);
                                            root.source(source.toString());
                                            return root;
                                        } catch (IOException e) {
                                            LOG.error("Failed to read json from source {}: {}", source, e);
                                            return null;
                                        }
                                    })
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toSet());
        mavenRepositories.addAll(dependencySpecs.stream()
                                                .flatMap((dep) -> dep.repositories().stream())
                                                .collect(Collectors.toSet()));
        val artifacts = dependencySpecs.stream()
                                       .map((root) -> new Pair<>(root.source(), root.dependencies()))
                                       .flatMap(pair -> flatMap(pair, (dep) -> Stream.of(dep.always(), Share.DEV_ENV ? dep.dev() : dep.obf())))
                                       .flatMap(pair -> flatMap(pair, (dep) -> Stream.concat(dep.common().stream(),
                                                                                             FMLLaunchHandler.side().isClient() ?
                                                                                             dep.client().stream() :
                                                                                             dep.server().stream())))
                                       .map((pair) -> {
                                           val source = pair.a;
                                           val dep = pair.b;
                                           val parts = dep.split(":");
                                           if (parts.length < 3) {
                                               LOG.error("Invalid dependency: {}", dep);
                                               return null;
                                           }
                                           val groupId = parts[0];
                                           val artifactId = parts[1];
                                           Version version;
                                           try {
                                               val matcher = VERSION_PATTERN.matcher(parts[2]);
                                               if (!matcher.matches()) {
                                                   throw new IllegalArgumentException("Invalid version: " + parts[2]);
                                               }
                                               val major = Integer.parseInt(matcher.group(1));
                                               val minor = matcher.group(2) == null ? -1 : Integer.parseInt(matcher.group(2));
                                               val patch = matcher.group(3) == null ? -1 : Integer.parseInt(matcher.group(3));
                                               val preRelease = matcher.group(4);
                                               val build = matcher.group(5);
                                               version = new SemanticVersion(major, minor, patch, preRelease, build);
                                           } catch (IllegalArgumentException e) {
                                               LOG.warn("Unparseable dependency version {}:{}:{} from {}", groupId, artifactId, parts[2], source);
                                               version = new RawVersion(parts[2]);
                                           }
                                           final String classifier = parts.length > 3 ? parts[3] : null;
                                           if (classifier != null) {
                                               LOG.info("Found dependency: {}:{}:{}:{} from {}", groupId, artifactId,
                                                        version, classifier, source);
                                           } else {
                                               LOG.info("Found dependency: {}:{}:{} from {}", groupId, artifactId,
                                                        version, source);
                                           }
                                           return new DependencyLoadTask(source, groupId, artifactId,
                                                                         version, null, version,
                                                                         classifier, classifier);
                                       })
                                       .filter(Objects::nonNull)
                                       .collect(Collectors.toSet());
        val artifactMap = new HashMap<String, DependencyLoadTask>();
        for (val artifact: artifacts) {
            val id = artifact.getGroupArtifact();
            if (artifactMap.containsKey(id)) {
                val otherArtifact = artifactMap.get(id);
                //TODO: Check for conflicts
                if (artifact.preferredVersion.compareTo(otherArtifact.preferredVersion) > 0) {
                    LOG.info("Replacing dependency {}:{} from {} with version {} from {}",
                             otherArtifact.getGroupArtifact(), otherArtifact.preferredVersion,
                             otherArtifact.loadingModId,
                             artifact.preferredVersion,
                             artifact.loadingModId);
                    artifactMap.put(id, artifact);
                }
            } else {
                artifactMap.put(id, artifact);
            }
        }

        val futures = new ArrayList<CompletableFuture<Void>>();
        for (val task : artifactMap.values()) {
            futures.add(CompletableFuture.runAsync(task::load, executor));
        }
        val theFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        theFuture.join();
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
            try {
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
            } catch (Exception e) {
                LOG.fatal(e);
            }
        }

        private void crashCouldNotDownload() {
            val errorMessage = "Failed to download library " + groupId + ":" + artifactId + ":" + preferredVersion +
                               ((suffix != null) ? ":" + suffix : "") + " from any repository! Requested by mod: " +
                               loadingModId;
            LOG.fatal(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        private void setupLibraryNames() {
            suffix = Share.DEV_ENV ? devSuffix : regularSuffix;
            artifactLogName = String.format("%s:%s:%s%s", groupId, artifactId, preferredVersion,
                                            suffix != null ? "-" + suffix : "");
            LOG.info("Adding library {}, requested by mod {}", artifactLogName, loadingModId);
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
                    LOG.fatal("ALERT VVVVVVVVVVVV ALERT");
                }
                LOG.fatal("Library {}:{}{} already loaded with version {}, " +
                          "but a version in the range {} was requested! Thing may go horribly wrong! " +
                          "Requested by mod: {}, previously loaded by mod: {}", groupId, artifactId,
                          suffix != null ? ":" + suffix : "", currentVer, rangeString, loadingModId,
                          loadedLibraryMods.get(artifact));
                for (int i = 0; i < 16; i++) {
                    LOG.fatal("ALERT ^^^^^^^^^^^^ ALERT");
                }
            } else {
                LOG.info("Attempted loading of library {}:{}{} with preferred version {}, " +
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
            val oldLibDir = new File(modsDir, "falsepattern");
            libDir = new File(homeDir, "falsepattern");
            mavenJarName =
                    String.format("%s-%s%s.jar", artifactId, preferredVersion, (suffix != null) ? ("-" + suffix) : "");
            jarName = groupId + "-" + mavenJarName;
            if (!libDir.exists()) {
                if (!libDir.mkdirs()) {
                    LOG.fatal("Failed to create directory {}", libDir);
                    throw new RuntimeException("Failed to create directory " + libDir);
                }
            }
            if (oldLibDir.exists()) {
                LOG.info("Migrating old library folder. From: " + oldLibDir.getAbsolutePath() + ", To: " + libDir.getAbsolutePath());
                val oldFiles = oldLibDir.listFiles();
                if (oldFiles != null) {
                    for (val file: oldFiles) {
                        try {
                            Files.move(file.toPath(), libDir.toPath().resolve(oldLibDir.toPath().relativize(file.toPath())), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            LOG.warn("Failed to move file " + file.getName() + " to new dir! Deleting instead.");
                            try {
                                Files.deleteIfExists(file.toPath());
                            } catch (IOException ex) {
                                LOG.warn("Failed to delete file " + file.getPath() + "!");
                                file.deleteOnExit();
                            }
                        }
                    }
                }
                try {
                    Files.deleteIfExists(oldLibDir.toPath());
                } catch (IOException e) {
                    LOG.warn("Failed to delete old library directory!");
                    oldLibDir.deleteOnExit();
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
                    LOG.debug("Library {} is missing checksum data! Either it was manually deleted, " +
                              "or the source repo didn't have it in the first place", artifactLogName);
                }
            } catch (IOException e) {
                LOG.error("Failed to execute validation check for " + artifactLogName, e);
                checkedDelete(file);
                return false;
            }
            try {
                addToClasspath(file);
                loadedLibraries.put(artifact, preferredVersion);
                LOG.debug("Library {} successfully loaded from disk!", artifactLogName);
                return true;
            } catch (RuntimeException e) {
                LOG.warn("Failed to load library {} from file! Re-downloading...", artifactLogName);
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
                LOG.fatal(errorMessage);
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
                                     (ex) -> LOG.debug("Artifact {} could not be downloaded from repo {}: {}",
                                                       artifactLogName, finalRepo, ex.getMessage()), (input) -> {
                                LOG.debug("Downloading {} from {}", artifactLogName, finalRepo);
                                download(input, file);
                                LOG.debug("Downloaded {} from {}", artifactLogName, finalRepo);
                                success.set(true);
                            });
                    if (success.get()) {
                        LOG.debug("Validating checksum for {}", artifactLogName);
                        val hadChecksum = validateChecksum(url);
                        switch (hadChecksum) {
                            case FAILED:
                                continue;
                            case OK:
                                break;
                            case MISSING:
                                LOG.warn("The library {} had no checksum available on the repository.\n" +
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
                LOG.debug("Attempting to get {} checksum...", checksumType);
                val success = new AtomicBoolean(false);
                Internet.connect(new URL(checksumURL),
                                 (ex) -> LOG.debug("Could not get {} checksum for {}: {}", checksumType,
                                                   artifactLogName, ex.getMessage()), (input) -> {
                            LOG.debug("Downloading {} checksum for {}", checksumType, artifactLogName);
                            download(input, checksumFile);
                            LOG.debug("Downloaded {} checksum for {}", checksumType, artifactLogName);
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
                LOG.debug("Attempting to read {} checksum from file...", checksumType);
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
                LOG.error("Failed {} checksum validation for {}.", checksumType, artifactLogName);
                checkedDelete(file);
                checkedDelete(checksumFile);
                return ChecksumStatus.FAILED;
            }
            LOG.debug("Successfully validated {} checksum for {}.", checksumType, artifactLogName);
            return ChecksumStatus.OK;
        }

        public String getGroupArtifact() {
            return groupId + ":" + artifactId;
        }

        private enum ChecksumStatus {
            OK,
            FAILED,
            MISSING
        }
    }
}
