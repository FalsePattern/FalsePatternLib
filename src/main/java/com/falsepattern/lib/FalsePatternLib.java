package com.falsepattern.lib;

import com.falsepattern.lib.api.Version;
import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import lombok.NonNull;
import lombok.val;
import lombok.var;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.*;


public class FalsePatternLib extends DummyModContainer {
    public static Logger libLog = LogManager.getLogger(ModInfo.MODNAME);
    public static final boolean developerEnvironment = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    private static final Map<String, Version> loadedLibraries = new HashMap<>();
    private static final Map<String, String> loadedLibraryMods = new HashMap<>();
    private static final Set<String> mavenRepositories = new HashSet<>();

    public FalsePatternLib() {
        super(new ModMetadata());
        libLog.info("All your libraries are belong to us!");
        val meta = getMetadata();
        meta.modId = ModInfo.MODID;
        meta.name = ModInfo.MODNAME;
        meta.version = ModInfo.VERSION;
        meta.url = ModInfo.URL;
        meta.credits = ModInfo.CREDITS;
        meta.authorList = Arrays.asList(ModInfo.AUTHORS);
        meta.description = ModInfo.DESCRIPTION;
        meta.useDependencyInformation = true;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

    public static void addMavenRepo(String url) {
        mavenRepositories.add(url);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void loadLibrary(String loadingModId, String groupId, String artifactId, @NonNull Version minVersion, Version maxVersion, @NonNull Version preferredVersion, String devSuffix) {
        libLog.info("Adding library {}:{}:{}, requested by mod {}", groupId, artifactId, preferredVersion, loadingModId);
        var artifact = groupId + ":" + artifactId;
        if (loadedLibraries.containsKey(artifact)) {
            val currentVer = loadedLibraries.get(artifact);
            if (currentVer.equals(preferredVersion)) return;
            val rangeString = "(minimum: " + minVersion + (maxVersion == null ? "" : ", maximum: " + maxVersion) + ")";
            if (minVersion.compareTo(currentVer) > 0 || (maxVersion != null && maxVersion.compareTo(currentVer) < 0)) {
                for (int i = 0; i < 16; i++) {
                    libLog.fatal("ALERT VVVVVVVVVVVV ALERT");
                }
                libLog.fatal("Library {}:{} already loaded with version {}, " +
                             "but a version in the range {} was requested! Thing may go horribly wrong! " +
                             "Requested by mod: {}, previously loaded by mod: {}",
                        groupId, artifactId, currentVer,
                        rangeString,
                        loadingModId, loadedLibraryMods.get(artifact));
                for (int i = 0; i < 16; i++) {
                    libLog.fatal("ALERT ^^^^^^^^^^^^ ALERT");
                }
            } else {
                libLog.info("Attempted loading of library {}:{} with preferred version {}, " +
                            "but version {} was already loaded, which matched the range {}. This is not an error. " +
                            "Requested by mod: {}, previously loaded by mod: {}",
                        groupId, artifactId, preferredVersion,
                        currentVer, rangeString,
                        loadingModId, loadedLibraryMods.get(artifact));
            }
            return;
        }
        val modsDir = new File(CoreLoadingPlugin.mcDir, "mods");
        val mavenJarName = String.format("%s-%s%s.jar", artifactId, preferredVersion, (developerEnvironment && devSuffix != null) ? ("-" + devSuffix) : "");
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
                libLog.info("Library {}:{}:{} successfully loaded from disk!", groupId, artifactId, preferredVersion);
                return;
            } catch (RuntimeException e) {
                libLog.warn("Failed to load library {}:{}:{} from file! Redownloading...", groupId, artifactId, preferredVersion);
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
                    libLog.info("Artifact {}:{}:{} was not found on repo {}", groupId, artifactId, preferredVersion, repo);
                    connection.disconnect();
                    continue;
                }
                libLog.info("Downloading {}:{}:{} from {}", groupId, artifactId, preferredVersion, repo);
                download(connection.getInputStream(), file);
                libLog.info("Downloaded {}:{}:{}", groupId, artifactId, preferredVersion);
                loadedLibraries.put(artifact, preferredVersion);
                loadedLibraryMods.put(artifact, loadingModId);
                addToClasspath(file);
                return;
            } catch (IOException ignored) {}
        }
        val errorMessage = "Failed to download library " + groupId + ":" + artifactId + ":" + preferredVersion + " from any repository! Requested by mod: " + loadingModId;
        libLog.fatal(errorMessage);
        throw new IllegalStateException(errorMessage);
    }

    private static void addToClasspath(File file) {
        try {
            val cl = (LaunchClassLoader) FalsePatternLib.class.getClassLoader();
            cl.addURL(file.toURI().toURL());
            libLog.info("Injected file {} into classpath!", file.getPath());
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
