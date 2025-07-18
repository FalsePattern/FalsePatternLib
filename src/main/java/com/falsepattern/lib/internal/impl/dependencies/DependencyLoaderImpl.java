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
package com.falsepattern.lib.internal.impl.dependencies;

import com.falsepattern.lib.dependencies.Library;
import com.falsepattern.lib.dependencies.SemanticVersion;
import com.falsepattern.lib.dependencies.Version;
import com.falsepattern.lib.internal.Internet;
import com.falsepattern.lib.internal.Share;
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.config.EarlyConfig;
import com.falsepattern.lib.internal.core.LowLevelCallMultiplexer;
import com.falsepattern.lib.util.FileUtil;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import io.netty.util.internal.ConcurrentSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;

import javax.swing.JFrame;
import javax.swing.JLabel;
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
import java.util.function.Function;
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
    private static final Map<String, Version> loadedLibraries = new ConcurrentHashMap<>();
    private static final Map<String, String> loadedLibraryMods = new ConcurrentHashMap<>();
    private static final Set<String> remoteMavenRepositories = new ConcurrentSet<>();
    private static final Set<String> localMavenRepositories = new ConcurrentSet<>();
    private static final Logger LOG = LogManager.getLogger(Tags.MODNAME + " Library Loader");

    private static final AtomicLong counter = new AtomicLong(0);
    private static final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        val thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("Dependency Download Thread " + counter.incrementAndGet());
        return thread;
    });
    private static final Path libDir;
    private static final Path tempDir;

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
        if (homeDirStr == null) {
            homeDir = FileUtil.getMinecraftHome().toPath();
        } else {
            homeDir = Paths.get(homeDirStr);
        }
        libDir = homeDir.resolve("falsepattern");
        tempDir = homeDir.resolve(Paths.get("logs", "falsepattern_tmp"));
        ensureExists(libDir);
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

    private static synchronized void addToClasspath(Path file) {
        try {
            LowLevelCallMultiplexer.addURLToClassPath(file.toUri().toURL());
            LOG.debug("Injected file {} into classpath!", file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add library to classpath: " + file.toAbsolutePath(), e);
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
        val futures = new ArrayList<CompletableFuture<Void>>();
        for (val library : libraries) {
            val task = new DependencyLoadTask(library.loadingModId,
                                              library.groupId,
                                              library.artifactId,
                                              library.minVersion,
                                              library.maxVersion,
                                              library.preferredVersion,
                                              library.regularSuffix,
                                              library.devSuffix);
            futures.add(CompletableFuture.runAsync(task::load, executor));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private static boolean scanForDepSpecs(URL source, List<URL> output, List<URL> jijURLs) {
        if (!source.getProtocol().equals("file")) {
            return false;
        }
        val fileName = source.getFile();
        boolean found = false;
        if (fileName.endsWith(".jar")) {
            //Scan jar file for json in META-INF, add them to the list
            try (val inputStream = new BufferedInputStream(source.openStream(), 65536);
                 val jarFile = new JarInputStream(inputStream, false)) {
                ZipEntry entry;
                while ((entry = jarFile.getNextEntry()) != null) {
                    val name = entry.getName();
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
            Path dir;
            try {
                dir = Paths.get(source.toURI());
            } catch (URISyntaxException e) {
                LOG.error("Could not scan URL " + source + " for dependencies", e);
                return false;
            }
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                LOG.warn("Skipping non-directory, nor jar source: {}", source);
                return false;
            }
            //Scan directory for json in META-INF, add them to the list
            val metaInf = dir.resolve("META-INF");
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

    private static <A, B, C> Stream<Pair<A, B>> flatMap(Pair<A, C> pair, Function<C, Stream<B>> mapper) {
        val a = pair.a;
        return mapper.apply(pair.b).map(b -> new Pair<>(a, b));
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

    private static Pair<DependencyScope, DepRoot.SidedDependencies> scopedDependency(DependencyScope scope, DepRoot.SidedDependencies deps) {
        return new Pair<>(scope, deps);
    }

    private static Stream<Pair<ScopeSide, String>> sidedDependency(DependencySide side, Stream<Pair<DependencyScope, String>> deps) {
        return deps.map(dep -> new Pair<>(new ScopeSide(dep.a, side), dep.b));
    }

    private static boolean initialScan = false;
    private static List<Pair<ScopeSide, DependencyLoadTask>> tasks;

    public static void executeDependencyLoading(boolean sideAware) {
        if (!initialScan) {
            initialScan = true;
            scanDeps();
        }

        executeArtifactLoading(sideAware);
    }

    private static void scanDeps() {
        LOG.debug("Discovering dependency source candidates...");
        val modsDir = FileUtil.getMinecraftHomePath().resolve("mods");
        val mods1710Dir = modsDir.resolve("1.7.10");
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
        val candidates = Stream.of(LowLevelCallMultiplexer.getClassPathSources().stream(),
                                   grabSourceCandidatesFromFolder(modsDir),
                                   grabSourceCandidatesFromFolder(mods1710Dir))
                               .flatMap((i) -> i)
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
                val gson = builder.create();
                json.remove("identifier");
                val root = gson.fromJson(json, DepRoot.class);
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
                                                      .flatMap((dep) -> dep.repositories().stream())
                                                      .map(repo -> repo.endsWith("/") ? repo : repo + "/")
                                                      .collect(Collectors.toSet()));
        localMavenRepositories.addAll(jijURLs.stream().map(URL::toString).map(repo -> repo.endsWith("/") ? repo : repo + "/").collect(Collectors.toSet()));
        val artifacts = dependencySpecs.stream()
                                       .map((root) -> new Pair<>(root.source(), root.dependencies()))
                                       .flatMap(pair -> flatMap(pair,
                                                                (dep) -> Stream.of(scopedDependency(DependencyScope.ALWAYS, dep.always()),
                                                                                   scopedDependency(DependencyScope.DEV, dep.dev()),
                                                                                   scopedDependency(DependencyScope.OBF, dep.obf())
                                                                                  )))
                                       .flatMap(pair -> flatMap(pair,
                                                                (dep) -> Stream.concat(sidedDependency(DependencySide.COMMON, flatMap(dep, d -> d.common().stream())),
                                                                                       Stream.concat(sidedDependency(DependencySide.CLIENT, flatMap(dep, d -> d.client().stream())),
                                                                                                     sidedDependency(DependencySide.SERVER, flatMap(dep, d -> d.server().stream())))
                                                                                       )))
                                       .map((pair) -> {
                                           val source = pair.a;
                                           val scopedDep = pair.b;
                                           val dep = scopedDep.b;
                                           val scope = scopedDep.a;
                                           val parts = dep.split(":");
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
                                           return new Pair<>(scope,
                                                             new DependencyLoadTask(source,
                                                                                    groupId,
                                                                                    artifactId,
                                                                                    version,
                                                                                    null,
                                                                                    version,
                                                                                    classifier,
                                                                                    classifier));
                                       })
                                       .filter(Objects::nonNull)
                                       .collect(Collectors.toSet());
        tasks = new ArrayList<>(artifacts);
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
            return new ScopeSide(Share.DEV_ENV ? DependencyScope.DEV : DependencyScope.OBF, FMLLaunchHandler.side() == Side.CLIENT ? DependencySide.CLIENT : DependencySide.SERVER);
        }
    }

    private static void executeArtifactLoading(boolean sideAware) {
        val scopeSide = sideAware ? SideAwareAssistant.current() : new ScopeSide(DependencyScope.ALWAYS, DependencySide.COMMON);
        val iter = tasks.iterator();
        val artifactMap = new HashMap<String, DependencyLoadTask>();
        while (iter.hasNext()) {
            val artifact = iter.next();
            val artifactSide = artifact.a;
            if (!scopeSide.contains(artifactSide))
                continue;
            iter.remove();
            val artifactPayload = artifact.b;
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
            return;

        val futures = new ArrayList<CompletableFuture<Void>>();
        LOG.info("-----------------------------------------------------------");
        LOG.info("FalsePatternLib is downloading dependencies. Please wait...");
        LOG.info("-----------------------------------------------------------");
        JFrame theFrame;
        val progresses = new HashMap<DependencyLoadTask, JProgressBar>();
        {
            JFrame jFrame = null;
            try {
                jFrame = new JFrame("Dependency Download");
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
                jFrame.setVisible(true);
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                jFrame.setLocation(dim.width/2-jFrame.getSize().width/2, dim.height/2-jFrame.getSize().height/2);
            } catch (Exception ignored) {
            }
            theFrame = jFrame;
        }
        if (theFrame != null) {
            for (val task : artifactMap.values()) {
                futures.add(CompletableFuture.runAsync(() -> {
                    val bar = progresses.get(task);
                    bar.setString("Downloading...");
                    task.load();
                    bar.setMinimum(0);
                    bar.setMaximum(1);
                    bar.setValue(1);
                    bar.setString("Completed!");
                }, executor));
            }
        } else {
            for (val task : artifactMap.values()) {
                futures.add(CompletableFuture.runAsync(task::load, executor));
            }
        }
        val theFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        AtomicBoolean doViz = new AtomicBoolean(true);
        if (theFrame != null) {
            final var vizThread = getVizThread(doViz, progresses, theFrame);
            vizThread.start();
        }
        try {
            theFuture.join();
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            throw new Error(t);
        } finally {
            if (theFrame != null) {
                doViz.set(false);
            }
        }
    }

    @NotNull
    private static Thread getVizThread(AtomicBoolean doViz, HashMap<DependencyLoadTask, JProgressBar> progresses, JFrame theFrame) {
        val vizThread = new Thread(() -> {
            while (doViz.get()) {
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
        public volatile String jarName;
        private Path file;

        public volatile long contentLength = -1;
        public volatile long downloaded = 0;

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
            for (val repo: localMavenRepositories) {
                if (tryDownloadFromMaven(repo)) {
                    return;
                }
            }
            validateDownloadsAllowed();
            for (var repo : remoteMavenRepositories) {
                if (tryDownloadFromMaven(repo)) {
                    return;
                }
            }
            crashCouldNotDownload();
        }

        private void crashCouldNotDownload() {
            val errorMessage = "Failed to download library " + groupId + ":" + artifactId + ":" + preferredVersion + (
                    (suffix != null) ? ":" + suffix : "") + " from any repository! Requested by mod: " + loadingModId;
            LOG.fatal(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        private void setupLibraryNames() {
            suffix = Share.DEV_ENV ? devSuffix : regularSuffix;
            artifactLogName = String.format("%s:%s:%s%s",
                                            groupId,
                                            artifactId,
                                            preferredVersion,
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
                LOG.fatal("Library {}:{}{} already loaded with version {}, "
                          + "but a version in the range {} was requested! Thing may go horribly wrong! "
                          + "Requested by mod: {}, previously loaded by mod: {}",
                          groupId,
                          artifactId,
                          suffix != null ? ":" + suffix : "",
                          currentVer,
                          rangeString,
                          loadingModId,
                          loadedLibraryMods.get(artifact));
                for (int i = 0; i < 16; i++) {
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
                         loadedLibraryMods.get(artifact));
            }
        }

        private void setupPaths() {
            mavenJarName =
                    String.format("%s-%s%s.jar", artifactId, preferredVersion, (suffix != null) ? ("-" + suffix) : "");
            jarName = groupId + "-" + mavenJarName;
            file = libDir.resolve(jarName);
        }

        private boolean tryLoadingExistingFile() {
            if (!Files.exists(file)) {
                return false;
            }
            try {
                val status = validateChecksum(file);
                if (status == ChecksumStatus.FAILED) {
                    return false;
                } else if (status == ChecksumStatus.MISSING) {
                    LOG.debug("Library {} is missing checksum data! Either it was manually deleted, "
                              + "or the source repo didn't have it in the first place", artifactLogName);
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
            if (!EarlyConfig.getInstance().enableLibraryDownloads()) {
                val errorMessage = "Failed to load library "
                                   + groupId
                                   + ":"
                                   + artifactId
                                   + ":"
                                   + preferredVersion
                                   + ((suffix != null) ? ":" + suffix : "")
                                   + ": "
                                   + Tags.MODNAME
                                   + " library downloading has been disabled in the config, and the library is not present "
                                   + "on disk! Requested by mod: "
                                   + loadingModId;
                LOG.fatal(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        }

        private static final Object mutex = new Object();
        private boolean tryDownloadFromMaven(String repo) {
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
                            try {
                                Files.move(tmpFile, file, StandardCopyOption.ATOMIC_MOVE);
                            } catch (AtomicMoveNotSupportedException ignored) {
                                Files.move(tmpFile, file);
                            }
                            LOG.debug("Validating checksum for {}", artifactLogName);
                            val hadChecksum = validateChecksum(url);
                            switch (hadChecksum) {
                                case FAILED:
                                    continue;
                                case OK:
                                    break;
                                case MISSING:
                                    LOG.warn("The library {} had no checksum available on the repository.\n"
                                             + "There's a chance it might have gotten corrupted during download,\n"
                                             + "but we're loading it anyways.", artifactLogName);
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
