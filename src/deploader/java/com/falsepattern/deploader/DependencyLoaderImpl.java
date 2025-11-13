/*
 * This file is part of FalsePatternLib.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalsePatternLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * FalsePatternLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalsePatternLib. If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.deploader;

import com.falsepattern.deploader.version.RawVersion;
import com.falsepattern.deploader.version.SemanticVersion;
import com.falsepattern.deploader.version.Version;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import io.netty.util.internal.ConcurrentSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import cpw.mods.fml.common.MetadataCollection;
import cpw.mods.fml.common.ModMetadata;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public class DependencyLoaderImpl {

    /**
     * Library checksum formats in a decreasing order of quality.
     */
    private static final String[] CHECKSUM_TYPES = new String[]{"sha512", "sha256", "sha1", "md5"};
    private static final Set<Path> alreadyScannedSources = new ConcurrentSet<>();
    private static final Map<String, Version> loadedLibraries = new ConcurrentHashMap<>();
    private static final Map<String, String> loadedLibraryMods = new ConcurrentHashMap<>();
    private static final Map<String, Version> loadedModIds = new ConcurrentHashMap<>();
    private static final Map<String, String> loadedModIdMods = new ConcurrentHashMap<>();
    private static final Set<String> remoteMavenRepositories = new ConcurrentSet<>();
    private static final Set<String> localMavenRepositories = new ConcurrentSet<>();
    static final Logger LOG = LogManager.getLogger("FalsePatternLib DepLoader");

    private static final AtomicLong counter = new AtomicLong(0);
    private static final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        val thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("Dependency Download Thread " + counter.incrementAndGet());
        return thread;
    });
    private static final Path libDir;
    private static final Path modsDir;
    private static final Path tempDir;
    private static final Field metadataCollectionModListAccessor;

    static {
        try {
            metadataCollectionModListAccessor = MetadataCollection.class.getDeclaredField("modList");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        metadataCollectionModListAccessor.setAccessible(true);
    }

    private static AtomicBoolean modDownloaded = new AtomicBoolean(false);

    private static void ensureExists(Path directory) {
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                LOG.fatal("Failed to create directory {}", directory);
                throw new RuntimeException("Failed to create directory " + directory, e);
            }
        }
    }

    static {
        Path homeDir;
        String homeDirStr = System.getProperty("minecraft.sharedDataDir");
        if (homeDirStr == null) {
            homeDirStr = System.getenv("MINECRAFT_SHARED_DATA_DIR");
        }
        Path mcHomeDir = Bootstrap.MINECRAFT_HOME_PATH;
        if (homeDirStr == null) {
            homeDir = mcHomeDir;
        } else {
            homeDir = Paths.get(homeDirStr);
        }
        libDir = homeDir.resolve("falsepattern");
        modsDir = mcHomeDir.resolve(Paths.get("mods", "1.7.10"));
        tempDir = homeDir.resolve(Paths.get("logs", "falsepattern_tmp"));
        ensureExists(libDir);
        ensureExists(modsDir);
        ensureExists(tempDir);
        val oldLibDir = homeDir.resolve(Paths.get("mods", "falsepattern"));
        if (Files.exists(oldLibDir)) {
            LOG.info("Migrating old library folder. From: "
                     + oldLibDir.toAbsolutePath()
                     + ", To: "
                     + libDir.toAbsolutePath());
            try (val oldFiles = Files.list(oldLibDir)) {
                oldFiles.forEach(file -> {
                    try {
                        Files.move(file,
                                   libDir.resolve(oldLibDir.relativize(file)),
                                   StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        LOG.warn("Failed to move file " + file.getFileName() + " to new dir! Deleting instead.");
                        try {
                            Files.deleteIfExists(file);
                        } catch (IOException ex) {
                            LOG.warn("Failed to delete file " + file + "!", ex);
                        }
                    }
                });
            } catch (IOException e) {
                LOG.warn("Failed to iterate old library directory!", e);
            }
            try {
                Files.deleteIfExists(oldLibDir);
            } catch (IOException e) {
                LOG.warn("Failed to delete old library directory!", e);
            }
        }
        var oldCacheFile = libDir.resolve(".depscan_cache");
        if (Files.exists(oldCacheFile)) {
            try {
                Files.delete(oldCacheFile);
            } catch (IOException e) {
                LOG.warn("Failed to delete old depscan cache file!", e);
            }
        }
    }

    public static void addMavenRepo(String url) {
        remoteMavenRepositories.add(url);
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
    private static String hash(String algo, Path file) {
        byte[] data = Files.readAllBytes(file);
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

    private static void checkedDelete(Path file) {
        try {
            Files.delete(file);
        } catch (IOException e) {
            LOG.fatal("Failed to delete file {}", file);
            throw new RuntimeException("Failed to delete file " + file, e);
        }
    }

    private static synchronized void addToClasspath(URL file) {
        try {
            LowLevelCallMultiplexer.addURLToClassPath(file);
            LOG.debug("Injected file {} into classpath!", file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add library to classpath: " + file, e);
        }
    }

    @SneakyThrows
    private static void download(InputStream is, Path target, Consumer<Integer> downloadSizeCallback) {
        if (Files.exists(target)) {
            return;
        }
        Internet.transferAndClose(is, new BufferedOutputStream(Files.newOutputStream(target)), downloadSizeCallback);
    }

    public static void loadLibraries(Library... libraries) {
        loadLibrariesAsync(libraries).join();
    }

    public static CompletableFuture<Void> loadLibrariesAsync(Library... libraries) {
        val futures = new ArrayList<CompletableFuture<URL>>();
        for (val library : libraries) {
            val task = new DependencyLoadTask(library.loadingModId,
                                              library.groupId,
                                              library.artifactId,
                                              library.minVersion,
                                              library.maxVersion,
                                              library.preferredVersion,
                                              library.regularSuffix,
                                              library.devSuffix,
                                              false,
                                              null);
            futures.add(CompletableFuture.supplyAsync(task::load, executor));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(ignored -> {
            var tasks = futures.stream().map(it -> it.getNow(null)).filter(Objects::nonNull).collect(Collectors.toSet());
            if (!tasks.isEmpty()) {
                var deps = scanDeps(tasks.stream());
                while (deps != null) {
                    deps = executeArtifactLoading(new ArrayList<>(deps), true);
                }
            }
        });
    }

    @SneakyThrows
    private static void readMCMod(InputStream input, String name) {
        val meta = MetadataCollection.from(input, name);
        val modList = (ModMetadata[])metadataCollectionModListAccessor.get(meta);
        for (val mod: modList) {
            val id = mod.modId;
            if (id != null) {
                val versionStr = mod.version;
                final Version version;
                if (versionStr != null) {
                    version = Version.parse(versionStr);
                } else {
                    version = new RawVersion("unknown");
                }
                loadedModIds.put(id, version);
                loadedModIdMods.put(id, name);
            }
        }
    }

    private static boolean scanForDepSpecs(URL source, List<URL> output, List<URL> jijURLs) {
        if (!source.getProtocol().equals("file")) {
            return false;
        }
        val fileName = source.getFile();
        Path path;

        try {
            path = Paths.get(source.toURI());
        } catch (URISyntaxException e) {
            LOG.error("Could not scan URL " + source + " for dependencies", e);
            return false;
        }
        if (alreadyScannedSources.contains(path))
            return true;
        alreadyScannedSources.add(path);
        boolean found = false;
        if (fileName.endsWith(".jar")) {
            //Scan jar file for json in META-INF, add them to the list
            try (val inputStream = new BufferedInputStream(source.openStream(), 65536);
                 val jarFile = new JarInputStream(inputStream, false)) {
                ZipEntry entry;
                while ((entry = jarFile.getNextEntry()) != null) {
                    val name = entry.getName();
                    if (name.equals("mcmod.info")) {
                        readMCMod(jarFile, source.toString());
                    }
                    if (!name.startsWith("META-INF/"))
                        continue;
                    if (name.endsWith(".json") && name.matches("META-INF/\\w+.json")) {
                        try {
                            output.add(new URL("jar:" + source + "!/" + entry.getName()));
                            found = true;
                        } catch (MalformedURLException e) {
                            LOG.error("Failed to add json source {} to dependency source list: {}", entry.getName(), e);
                        }
                    } else if (name.equals("META-INF/falsepatternlib_repo/")) {
                        try {
                            jijURLs.add(new URL("jar:" + source + "!/" + entry.getName()));
                        } catch (MalformedURLException e) {
                            LOG.error("Failed to add jar-in-jar repo {}: {}", entry.getName(), e);
                        }
                    }
                }
            } catch (IOException e) {
                LOG.error("Failed to open jar file {}", source.getPath());
            }
        } else {
            if (!Files.exists(path) || !Files.isDirectory(path)) {
                LOG.warn("Skipping non-directory, nor jar source: {}", source);
                return false;
            }
            //Scan directory for json in META-INF, add them to the list
            val metaInf = path.resolve("META-INF");
            if (!Files.exists(metaInf) || !Files.isDirectory(metaInf)) {
                return false;
            }
            try (val files = Files.list(metaInf)) {
                found = files.reduce(false, (prev,file) -> {
                    val entryFileName = file.getFileName().toString();
                    if (entryFileName.endsWith(".json")) {
                        try {
                            output.add(file.toUri().toURL());
                            return true;
                        } catch (MalformedURLException e) {
                            LOG.error("Failed to add json source {} to dependency source list: {}",
                                      file.toString(),
                                      e);
                        }
                    } else if (entryFileName.equals("falsepatternlib_repo")) {
                        try {
                            jijURLs.add(file.toUri().toURL());
                        } catch (MalformedURLException e) {
                            LOG.error("Failed to add jar-in-jar repo {}: {}", file.toString(), e);
                        }
                    }
                    return prev;
                }, (a,b) -> a || b);
            } catch (IOException e) {
                LOG.error("Failed to open directory {}", metaInf);
            }
        }
        return found;
    }

    private static Stream<URL> grabSourceCandidatesFromFolder(Path folder) {
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            return Stream.empty();
        }
        Stream<Path> paths;
        try (val files = Files.list(folder)) {
            //Lazy loading is bad here
            paths = files.collect(Collectors.toSet()).stream();
        } catch (IOException ignored) {
            return Stream.empty();
        }

        return paths.map(file -> {
            try {
                return file.toUri().toURL();
            } catch (MalformedURLException e) {
                return null;
            }
        }).filter(Objects::nonNull);
    }

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("(0|[1-9]\\d*)" +
                            "(?:\\.(0|[1-9]\\d*))?" +
                            "(?:\\.(0|[1-9]\\d*))?" +
                            "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?" +
                            "(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?");

    private enum DependencySide {
        COMMON,
        CLIENT,
        SERVER
    }

    private enum DependencyScope {
        ALWAYS,
        DEV,
        OBF
    }

    @RequiredArgsConstructor
    private static class ScopeSide {
        final DependencyScope scope;
        final DependencySide side;

        boolean contains(ScopeSide dependency) {
            return  (dependency.scope == scope || dependency.scope == DependencyScope.ALWAYS) &&
                    (dependency.side == side || dependency.side == DependencySide.COMMON);
        }
    }

    private static boolean initialScan = false;
    private static final List<ScopedSidedTask> baseTasks = new ArrayList<>();

    public static void executeDependencyLoading() {
        if (!initialScan) {
            initialScan = true;
            val tasks = scanDeps();
            if (tasks != null) {
                baseTasks.addAll(tasks);
            }
        }

        var tasks = executeArtifactLoading(baseTasks, false);
        while (tasks != null) {
            baseTasks.addAll(tasks);
            tasks = executeArtifactLoading(baseTasks, false);
        }
        if (modDownloaded.get()) {
            if (!SystemUtils.IS_OS_MAC) {
                try {
                    JOptionPane.showMessageDialog(null, "A minecraft mod has been downloaded by the FalsePatternLib dependency downloader.\nYou must restart the game to apply these changes.", "Mod Dependency Download notice", JOptionPane.WARNING_MESSAGE);
                } catch (Exception ignored) {}
            }
            val msg = "A minecraft mod has been downloaded by the FalsePatternLib dependency downloader, and requires a game restart to get installed properly.";
            for (int i = 0; i < 16; i++) {
                LOG.warn(msg);
            }
            System.exit(0);
            throw new ModDependencyDownloaded(msg);
        }
    }

    private static class ModDependencyDownloaded extends Error {
        public ModDependencyDownloaded(String message) {
            super(message);
        }
    }

    private static @Nullable Set<ScopedSidedTask> scanDeps() {
        LOG.debug("Discovering dependency source candidates...");
        val modsDir = Bootstrap.MINECRAFT_HOME_PATH.resolve("mods");
        val mods1710Dir = modsDir.resolve("1.7.10");
        return scanDeps(Stream.of(LowLevelCallMultiplexer.getClassPathSources().stream(),
                                  grabSourceCandidatesFromFolder(modsDir),
                                  grabSourceCandidatesFromFolder(mods1710Dir))
                              .flatMap(i -> i));
    }

    private static @Nullable Set<ScopedSidedTask> scanDeps(Stream<URL> candidatesUnfiltered) {
        long start = System.currentTimeMillis();
        val urlsWithoutDeps = new HashSet<String>();
        val depCache = tempDir.resolve(".depscan_cache");
        if (Files.exists(depCache)) {
            try {
                urlsWithoutDeps.addAll(Files.readAllLines(depCache));
            } catch (IOException e) {
                LOG.error("Could not read dependency scanner cache", e);
            }
        }
        val candidates = candidatesUnfiltered
                .filter(Objects::nonNull)
                .filter((url) -> !urlsWithoutDeps.contains(url.toString()))
                .collect(Collectors.toList());
        val urls = new ArrayList<URL>();
        val jijURLs = new ArrayList<URL>();
        for (val candidate : candidates) {
            if (!scanForDepSpecs(candidate, urls, jijURLs)) {
                urlsWithoutDeps.add(candidate.toString());
            }
        }
        try (val out = Files.newBufferedWriter(depCache)) {
            for (val noDep : urlsWithoutDeps) {
                out.append(noDep).append(System.lineSeparator());
            }
        } catch (IOException e) {
            LOG.error("Could not write dependency scanner cache", e);
        }
        val javaVersion = LowLevelCallMultiplexer.javaMajorVersion();
        val dependencySpecs = urls.stream().map((source) -> {
            //Convert source to GSON json
            try (val is = new BufferedInputStream(source.openStream())) {
                val jsonRaw = new JsonParser().parse(new InputStreamReader(is));
                if (!jsonRaw.isJsonObject()) {
                    return null;
                }
                val json = jsonRaw.getAsJsonObject();
                if (!(json.has("identifier") && json.get("identifier")
                                                    .getAsString()
                                                    .equals("falsepatternlib_dependencies"))) {
                    return null;
                }
                val builder = new GsonBuilder();
                builder.excludeFieldsWithoutExposeAnnotation();
                builder.registerTypeAdapterFactory(new DepRoot.Dependency.Adapter.Factory());
                val gson = builder.create();
                json.remove("identifier");
                val root = gson.fromJson(json, DepRoot.class);
                val minJ = root.minJava();
                val maxJ = root.maxJava();
                if (minJ != null && minJ > javaVersion || maxJ != null && maxJ < javaVersion) {
                    return null;
                }
                root.source(source.toString());
                return root;
            } catch (Exception e) {
                LOG.error("Failed to read json from source {}: {}", source, e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        long end = System.currentTimeMillis();
        LOG.debug("Discovered {} dependency source candidates in {}ms", dependencySpecs.size(), end - start);
        remoteMavenRepositories.addAll(dependencySpecs.stream()
                                                      .map(DepRoot::repositories)
                                                      .filter(Objects::nonNull)
                                                      .flatMap(Collection::stream)
                                                      .map(repo -> repo.endsWith("/") ? repo : repo + "/")
                                                      .collect(Collectors.toSet()));
        localMavenRepositories.addAll(jijURLs.stream().map(URL::toString).map(repo -> repo.endsWith("/") ? repo : repo + "/").collect(Collectors.toSet()));
        for (DepRoot dependencySpec : dependencySpecs) {
            val bundled = dependencySpec.bundledArtifacts();
            if (bundled == null) {
                continue;
            }
            for (var dep : bundled) {
                val parts = dep.artifact().split(":");
                if (parts.length < 3) {
                    LOG.error("Invalid bundled artifact: {}", dependencySpec);
                    continue;
                }
                val groupId = parts[0];
                val artifactId = parts[1];
                Version version = parseVersion(parts[2]);
                final String classifier = parts.length > 3 ? parts[3] : null;
                if (classifier != null) {
                    LOG.info("Found bundled artifact: {}:{}:{}:{} from {}",
                             groupId,
                             artifactId,
                             version,
                             classifier,
                             dependencySpec.source());
                } else {
                    LOG.info("Found bundled artifact: {}:{}:{} from {}",
                             groupId,
                             artifactId,
                             version,
                             dependencySpec.source());
                }
                val id = groupId + ":" + artifactId + ":" + classifier;
                val src = dependencySpec.source();
                if (!loadedLibraries.containsKey(id)) {
                    loadedLibraries.put(id, version);
                }
                if (!loadedLibraryMods.containsKey(id)) {
                    loadedLibraryMods.put(id, src);
                }
                val modid = dep.modid();
                if (modid != null) {
                    LOG.info("With modid: {}", modid);
                    if (!loadedModIds.containsKey(modid)) {
                        loadedModIds.put(modid, version);
                    }
                    if (!loadedModIdMods.containsKey(modid)) {
                        loadedModIdMods.put(modid, src);
                    }
                }
            }
        }
        val artifacts = dependencySpecs.stream()
                                       .flatMap(it -> Stream.of(
                                               new SourcedDep(it.source(), it.dependencies(), false),
                                               new SourcedDep(it.source(), it.modDependencies(), true)))
                                       .filter(it -> it.dep != null)
                                       .flatMap(it -> Stream.of(
                                               new ScopedDep(it.source, it.mod, DependencyScope.ALWAYS, it.dep.always()),
                                               new ScopedDep(it.source, it.mod, DependencyScope.DEV, it.dep.dev()),
                                               new ScopedDep(it.source, it.mod, DependencyScope.OBF, it.dep.obf())))
                                       .filter(it -> it.deps != null)
                                       .flatMap(it -> {
                                           Stream<ScopedSidedDep> result = null;
                                           result = concat(it, result, it.deps.common(), DependencySide.COMMON);
                                           result = concat(it, result, it.deps.client(), DependencySide.CLIENT);
                                           result = concat(it, result, it.deps.server(), DependencySide.SERVER);
                                           return result;
                                       })
                                       .map((scopedSidedDep) -> {
                                           val source = scopedSidedDep.source;
                                           val scope = scopedSidedDep.scope;
                                           val dep = scopedSidedDep.dep;
                                           val parts = dep.artifact().split(":");
                                           if (parts.length < 3) {
                                               LOG.error("Invalid dependency: {}", dep);
                                               return null;
                                           }
                                           val groupId = parts[0];
                                           val artifactId = parts[1];
                                           Version version = parseVersion(parts[2]);
                                           final String classifier = parts.length > 3 ? parts[3] : null;
                                           if (classifier != null) {
                                               LOG.info("Found dependency: {}:{}:{}:{} from {}",
                                                        groupId,
                                                        artifactId,
                                                        version,
                                                        classifier,
                                                        source);
                                           } else {
                                               LOG.info("Found dependency: {}:{}:{} from {}",
                                                        groupId,
                                                        artifactId,
                                                        version,
                                                        source);
                                           }
                                           return new ScopedSidedTask(scope,
                                                                      new DependencyLoadTask(source,
                                                                                             groupId,
                                                                                             artifactId,
                                                                                             version,
                                                                                             null,
                                                                                             version,
                                                                                             classifier,
                                                                                             classifier,
                                                                                             scopedSidedDep.mod,
                                                                                             dep.modid()));
                                       })
                                       .filter(Objects::nonNull)
                                       .collect(Collectors.toSet());
        if (artifacts.isEmpty()) {
            return null;
        }
        return artifacts;
    }

    private static Stream<ScopedSidedDep> concat(ScopedDep it, @Nullable Stream<ScopedSidedDep> prev, List<DepRoot.Dependency> deps, DependencySide side) {
        if (deps != null) {
            val newStream = deps.stream().map(dep -> new ScopedSidedDep(it.source, it.mod, new ScopeSide(it.scope, side), dep));
            if (prev == null) {
                return newStream;
            }
            return Stream.concat(prev, newStream);
        }
        return prev;
    }

    public static Version parseVersion(String versionString) {
        try {
            val matcher = VERSION_PATTERN.matcher(versionString);
            if (!matcher.matches()) {
                return new RawVersion(versionString);
            }
            val major = Integer.parseInt(matcher.group(1));
            val minor = matcher.group(2) == null ? -1
                                                 : Integer.parseInt(matcher.group(2));
            val patch = matcher.group(3) == null ? -1
                                                 : Integer.parseInt(matcher.group(3));
            val preRelease = matcher.group(4);
            val build = matcher.group(5);
            return new SemanticVersion(major, minor, patch, preRelease, build);
        } catch (Exception e) {
            return new RawVersion(versionString);
        }
    }

    private static class SideAwareAssistant {
        static ScopeSide current() {
            return new ScopeSide(Share.DEV_ENV ? DependencyScope.DEV : DependencyScope.OBF, Share.CLIENT ? DependencySide.CLIENT : DependencySide.SERVER);
        }
    }

    private static @Nullable Set<ScopedSidedTask> executeArtifactLoading(List<ScopedSidedTask> tasks, boolean silent) {
        val scopeSide = SideAwareAssistant.current();
        val iter = tasks.iterator();
        val artifactMap = new HashMap<String, DependencyLoadTask>();
        while (iter.hasNext()) {
            val artifact = iter.next();
            val artifactSide = artifact.scopeSide;
            if (!scopeSide.contains(artifactSide))
                continue;
            iter.remove();
            val artifactPayload = artifact.task;
            val id = artifactPayload.getGroupArtifact();
            if (artifactMap.containsKey(id)) {
                val otherArtifact = artifactMap.get(id);
                //TODO: Check for conflicts
                if (artifactPayload.preferredVersion.compareTo(otherArtifact.preferredVersion) > 0) {
                    LOG.info("Replacing dependency {}:{} from {} with version {} from {}",
                             otherArtifact.getGroupArtifact(),
                             otherArtifact.preferredVersion,
                             otherArtifact.loadingModId,
                             artifactPayload.preferredVersion,
                             artifactPayload.loadingModId);
                    artifactMap.put(id, artifactPayload);
                }
            } else {
                artifactMap.put(id, artifactPayload);
            }
        }
        if (artifactMap.isEmpty())
            return null;

        val futures = new ArrayList<CompletableFuture<URL>>();
        if (!silent) {
            LOG.info("-----------------------------------------------------------");
            LOG.info("FalsePatternLib is downloading dependencies. Please wait...");
            LOG.info("-----------------------------------------------------------");
        }
        JFrame theFrame = null;
        val progresses = new HashMap<DependencyLoadTask, JProgressBar>();
        if (silent) {

        } else if (SystemUtils.IS_OS_MAC) {
            LOG.info("MacOS detected, not creating progress window (your OS is buggy)");
        } else {
            try {
                val jFrame = new JFrame("Dependency Download");
                val constraints = new GridBagConstraints();
                jFrame.getContentPane().setLayout(new GridBagLayout());
                constraints.gridy = 0;
                constraints.gridwidth = 2;
                jFrame.add(new JLabel("FalsePatternLib is downloading dependencies, please wait!"), constraints);
                constraints.gridwidth = 1;
                for (val artifact : artifactMap.entrySet()) {
                    constraints.gridy++;
                    jFrame.add(new JLabel(artifact.getKey()), constraints);
                    val status = new JProgressBar();
                    status.setIndeterminate(true);
                    status.setStringPainted(true);
                    status.setString("Waiting...");
                    progresses.put(artifact.getValue(), status);
                    jFrame.add(status, constraints);
                }
                jFrame.pack();
                jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                jFrame.setLocation(dim.width/2-jFrame.getSize().width/2, dim.height/2-jFrame.getSize().height/2);
                theFrame = jFrame;
            } catch (Exception ignored) {
            }
        }
        if (theFrame != null) {
            for (val task : artifactMap.values()) {
                futures.add(CompletableFuture.supplyAsync(() -> {
                    val bar = progresses.get(task);
                    bar.setString("Downloading...");
                    val res = task.load();
                    bar.setMinimum(0);
                    bar.setMaximum(1);
                    bar.setValue(1);
                    bar.setString("Completed!");
                    return res;
                }, executor));
            }
        } else {
            for (val task : artifactMap.values()) {
                futures.add(CompletableFuture.supplyAsync(task::load, executor));
            }
        }
        val theFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                                         .thenApply(ignored ->
                                                            futures.stream()
                                                                   .map(it -> it.getNow(null))
                                                                   .collect(Collectors.toList())
                                                   );
        AtomicBoolean doViz = new AtomicBoolean(true);
        if (theFrame != null) {
            final var vizThread = getVizThread(doViz, progresses, theFrame);
            vizThread.start();
        }
        List<URL> res;
        try {
            res = theFuture.join();
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            throw new Error(t);
        } finally {
            if (theFrame != null) {
                doViz.set(false);
            }
        }
        return scanDeps(res.stream());
    }

    @NotNull
    private static Thread getVizThread(AtomicBoolean doViz, HashMap<DependencyLoadTask, JProgressBar> progresses, JFrame theFrame) {
        val vizThread = new Thread(() -> {
            int waitBeforeShowing = 100;
            while (doViz.get()) {
                if (waitBeforeShowing > 0) {
                    waitBeforeShowing--;
                } else if (waitBeforeShowing == 0) {
                    theFrame.setVisible(true);
                    waitBeforeShowing = -1;
                }
                for (val progress : progresses.entrySet()) {
                    val task = progress.getKey();
                    val bar = progress.getValue();
                    if (task.contentLength == -1) {
                        bar.setIndeterminate(true);
                    } else {
                        bar.setIndeterminate(false);
                        bar.setMinimum(0);
                        bar.setMaximum((int) task.contentLength);
                        bar.setValue((int) task.downloaded);
                    }
                }
                theFrame.repaint();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
            }
            theFrame.dispose();
        });
        vizThread.setDaemon(true);
        vizThread.setName("FalsePatternLib Download Visualizer");
        return vizThread;
    }

    @RequiredArgsConstructor
    private static class SourcedDep {
        public final String source;
        public final DepRoot.Dependencies dep;
        public final boolean mod;
    }

    @RequiredArgsConstructor
    private static class ScopedDep {
        public final String source;
        public final boolean mod;
        public final DependencyScope scope;
        public final DepRoot.SidedDependencies deps;
    }

    @RequiredArgsConstructor
    private static class ScopedSidedDep {
        public final String source;
        public final boolean mod;
        public final ScopeSide scope;
        public final DepRoot.Dependency dep;
    }

    @RequiredArgsConstructor
    private static class ScopedSidedTask {
        public final ScopeSide scopeSide;
        public final DependencyLoadTask task;
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
        private final boolean isMod;
        private final String modId;

        private String suffix;
        private String artifactLogName;
        private String artifact;
        private String mavenJarName;
        public volatile String jarName;
        private Path file;

        public volatile long contentLength = -1;
        public volatile long downloaded = 0;

        private @Nullable URL load() {
            setupLibraryNames();
            if (loadedLibraries.containsKey(artifact)) {
                alreadyLoaded(false);
                return null;
            }
            if (isMod && loadedModIds.containsKey(modId)) {
                alreadyLoaded(true);
                return null;
            }
            setupPaths();
            for (val repo: localMavenRepositories) {
                val url = tryDownloadFromMaven(repo, true);
                if (url != null) {
                    return url;
                }
            }
            val existingUrl = tryLoadingExistingFile();
            if (existingUrl != null) {
                return existingUrl;
            }
            validateDownloadsAllowed();
            for (var repo : remoteMavenRepositories) {
                val url = tryDownloadFromMaven(repo, false);
                if (url != null) {
                    return url;
                }
            }
            throw crashCouldNotDownload();
        }

        private IllegalStateException crashCouldNotDownload() {
            val errorMessage = "Failed to download library " + groupId + ":" + artifactId + ":" + preferredVersion + (
                    (suffix != null) ? ":" + suffix : "") + " from any repository! Requested by mod: " + loadingModId;
            LOG.fatal(errorMessage);
            return new IllegalStateException(errorMessage);
        }

        private void setupLibraryNames() {
            suffix = Share.DEV_ENV ? devSuffix : regularSuffix;
            artifactLogName = String.format("%s:%s:%s%s",
                                            groupId,
                                            artifactId,
                                            preferredVersion,
                                            suffix != null ? "-" + suffix : "");
            if (!isMod) {
                LOG.info("Adding library {}, requested by mod {}", artifactLogName, loadingModId);
            }
            artifact = groupId + ":" + artifactId + ":" + suffix;
        }

        private void alreadyLoaded(boolean fromModId) {
            val currentVer = fromModId ? loadedModIds.get(modId) : loadedLibraries.get(artifact);
            if (currentVer.equals(preferredVersion)) {
                return;
            }
            val rangeString = "(minimum: " + minVersion + (maxVersion == null ? "" : ", maximum: " + maxVersion) + ")";
            if (minVersion.compareTo(currentVer) > 0 || (maxVersion != null && maxVersion.compareTo(currentVer) < 0)) {
                for (int i = 0; i < 4; i++) {
                    LOG.fatal("ALERT VVVVVVVVVVVV ALERT");
                }
                LOG.fatal("Library {}:{}{} already loaded with version {}, "
                          + "but a version in the range {} was requested! Thing may go horribly wrong! "
                          + "Requested by mod: {}, previously loaded by mod: {}",
                          groupId,
                          artifactId,
                          suffix != null ? ":" + suffix : "",
                          currentVer,
                          rangeString,
                          loadingModId,
                          fromModId ? loadedModIdMods.get(modId) : loadedLibraryMods.get(artifact));
                for (int i = 0; i < 4; i++) {
                    LOG.fatal("ALERT ^^^^^^^^^^^^ ALERT");
                }
            } else {
                LOG.info("Attempted loading of library {}:{}{} with preferred version {}, "
                         + "but version {} was already loaded, which matched the range {}. This is not an "
                         + "error. "
                         + "Requested by mod: {}, previously loaded by mod: {}",
                         groupId,
                         artifactId,
                         suffix != null ? ":" + suffix : "",
                         preferredVersion,
                         currentVer,
                         rangeString,
                         loadingModId,
                         fromModId ? loadedModIdMods.get(modId) : loadedLibraryMods.get(artifact));
            }
        }

        private void setupPaths() {
            mavenJarName =
                    String.format("%s-%s%s.jar", artifactId, preferredVersion, (suffix != null) ? ("-" + suffix) : "");
            jarName = groupId + "-" + mavenJarName;
            file = (isMod ? modsDir : libDir).resolve(jarName);
        }

        private @Nullable URL tryLoadingExistingFile() {
            if (!Files.exists(file)) {
                return null;
            }
            try {
                val status = validateChecksum(file);
                if (status == ChecksumStatus.FAILED) {
                    return null;
                } else if (status == ChecksumStatus.MISSING) {
                    LOG.debug("Library {} is missing checksum data! Either it was manually deleted, "
                              + "or the source repo didn't have it in the first place", artifactLogName);
                }
            } catch (IOException e) {
                LOG.error("Failed to execute validation check for " + artifactLogName, e);
                checkedDelete(file);
                return null;
            }
            try {
                val theUrl = file.toUri().toURL();
                if (!isMod) {
                    addToClasspath(theUrl);
                }
                loadedLibraries.put(artifact, preferredVersion);
                if (isMod && modId != null) {
                    loadedModIds.put(modId, preferredVersion);
                }
                LOG.debug("Library {} successfully loaded from disk!", artifactLogName);
                return theUrl;
            } catch (Exception e) {
                LOG.warn("Failed to load library {} from file! Re-downloading...", artifactLogName);
                checkedDelete(file);
                return null;
            }
        }

        private void validateDownloadsAllowed() {
            if (!EarlyConfig.getInstance().enableLibraryDownloads()) {
                val errorMessage = "Failed to load library "
                                   + groupId
                                   + ":"
                                   + artifactId
                                   + ":"
                                   + preferredVersion
                                   + ((suffix != null) ? ":" + suffix : "")
                                   + ": "
                                   + "FalsePatternLib library downloading has been disabled in the config, and the library is not present "
                                   + "on disk! Requested by mod: "
                                   + loadingModId;
                LOG.fatal(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        }

        private static final Object mutex = new Object();
        private @Nullable URL tryDownloadFromMaven(String repo, boolean local) {
            synchronized (mutex) {
                try {
                    if (!repo.endsWith("/")) {
                        repo = repo + "/";
                    }
                    val url = String.format("%s%s/%s/%s/%s",
                                            repo,
                                            groupId.replace('.', '/'),
                                            artifactId,
                                            preferredVersion,
                                            mavenJarName);
                    String finalRepo = repo;
                    int retryCount = 0;
                    while (true) {
                        retryCount++;
                        if (retryCount > 3) {
                            break;
                        }
                        val success = new AtomicBoolean(false);
                        val tmpFile = file.getParent().resolve(file.getFileName().toString() + ".tmp");
                        if (Files.exists(tmpFile)) {
                            Files.delete(tmpFile);
                        }
                        Internet.connect(new URL(url),
                                         ex -> LOG.debug("Artifact {} could not be downloaded from repo {}: {}",
                                                         artifactLogName,
                                                         finalRepo,
                                                         ex.getMessage()),
                                         input -> {
                                             LOG.debug("Downloading {} from {}", artifactLogName, finalRepo);
                                             download(input, tmpFile, d -> downloaded += d);
                                             LOG.debug("Downloaded {} from {}", artifactLogName, finalRepo);
                                             success.set(true);
                                         },
                                         contentLength -> this.contentLength = contentLength);
                        if (success.get()) {
                            if (FileUtils.contentEquals(tmpFile.toFile(), file.toFile())) {
                                Files.delete(tmpFile);
                            } else {
                                if (isMod) {
                                    modDownloaded.set(true);
                                }
                                try {
                                    Files.move(tmpFile, file, StandardCopyOption.ATOMIC_MOVE);
                                } catch (AtomicMoveNotSupportedException ignored) {
                                    Files.move(tmpFile, file);
                                }
                            }
                            LOG.debug("Validating checksum for {}", artifactLogName);
                            val hadChecksum = validateChecksum(url);
                            switch (hadChecksum) {
                                case FAILED:
                                    continue;
                                case OK:
                                    break;
                                case MISSING:
                                    if (!local) {
                                        LOG.warn("The library {} had no checksum available on the repository.\n"
                                                 + "There's a chance it might have gotten corrupted during download,\n"
                                                 + "but we're loading it anyways.", artifactLogName);
                                    }
                            }
                            loadedLibraries.put(artifact, preferredVersion);
                            loadedLibraryMods.put(artifact, loadingModId);
                            val diskUrl = file.toUri().toURL();
                            if (!isMod) {
                                addToClasspath(diskUrl);
                            }
                            if (isMod && modId != null) {
                                loadedModIds.put(modId, preferredVersion);
                                loadedModIdMods.put(modId, loadingModId);
                            }
                            return diskUrl;
                        }
                    }
                } catch (IOException ignored) {
                }
                return null;
            }
        }

        private ChecksumStatus validateChecksum(String url) throws IOException {
            for (val checksumType : CHECKSUM_TYPES) {
                val checksumURL = url + "." + checksumType;
                val checksumFile = libDir.resolve(jarName + "." + checksumType);
                LOG.debug("Attempting to get {} checksum...", checksumType);
                val success = new AtomicBoolean(false);
                Internet.connect(new URL(checksumURL),
                                 (ex) -> LOG.debug("Could not get {} checksum for {}: {}",
                                                   checksumType,
                                                   artifactLogName,
                                                   ex.getMessage()),
                                 (input) -> {
                                     LOG.debug("Downloading {} checksum for {}", checksumType, artifactLogName);
                                     download(input, checksumFile, d -> {});
                                     LOG.debug("Downloaded {} checksum for {}", checksumType, artifactLogName);
                                     success.set(true);
                                 },
                                 length -> {});
                if (success.get()) {
                    return getChecksumStatus(file, checksumType, checksumFile);
                }
            }
            return ChecksumStatus.MISSING;
        }

        private ChecksumStatus validateChecksum(Path file) throws IOException {
            for (val checksumType : CHECKSUM_TYPES) {
                val checksumFile = libDir.resolve(jarName + "." + checksumType);
                LOG.debug("Attempting to read {} checksum from file...", checksumType);
                if (Files.exists(checksumFile)) {
                    return getChecksumStatus(file, checksumType, checksumFile);
                }
            }
            return ChecksumStatus.MISSING;
        }

        private ChecksumStatus getChecksumStatus(Path file, String checksumType, Path checksumFile) throws IOException {
            val fileHash = hash(checksumType, file);
            val referenceHash = new String(Files.readAllBytes(checksumFile));
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
