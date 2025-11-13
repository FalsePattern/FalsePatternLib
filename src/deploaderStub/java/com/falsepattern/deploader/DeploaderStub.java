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

import com.gtnewhorizons.retrofuturabootstrap.RfbApiImpl;
import com.gtnewhorizons.retrofuturabootstrap.api.ExtensibleClassLoader;
import org.apache.commons.io.IOUtils;

import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public final class DeploaderStub {
    private static final String BLACKBOARD_MARKER = "FPLIB_DEPLOADER";
    private static final String BLACKBOARD_RUN_DEPLOADER_MARKER = "FPLIB_RUN_DEPLOADER";
    private static boolean rfbDetected = false;
    private int version = -1;
    private Path path = null;
    //Call this from the static initializer of your coremod / RFB coremod
    public static void bootstrap(boolean rfb) {
        if (rfb) {
            rfbDetected = true;
            RFBUtil.preinit();
        }
        Object deploader = Launch.blackboard.get(BLACKBOARD_MARKER);
        if (deploader != null) {
            return;
        }
        DeploaderStub scanner = new DeploaderStub();
        @SuppressWarnings("resource")
        URLClassLoader cl = classLoader();
        for (URL url: cl.getURLs()) {
            try {
                scanner.scanCandidate(new File(url.toURI()).toPath());
            } catch (Exception ignored) {
            }
        }
        Path mcHomeDir = gameDir();
        Path mods = mcHomeDir.resolve("mods");
        Path mods1710 = mods.resolve("1.7.10");
        scanner.scanCandidates(mods);
        scanner.scanCandidates(mods1710);
        if (scanner.path == null) {
            throw new IllegalStateException("Failed to find fplib deploader! Are you sure you bundled it in your jar?");
        }

        Path homeDir;
        String homeDirStr = System.getProperty("minecraft.sharedDataDir");
        if (homeDirStr == null) {
            homeDirStr = System.getenv("MINECRAFT_SHARED_DATA_DIR");
        }
        if (homeDirStr == null) {
            homeDir = mcHomeDir;
        } else {
            homeDir = Paths.get(homeDirStr);
        }
        Path libDir = homeDir.resolve("falsepattern");
        if (!Files.exists(libDir)) {
            try {
                Files.createDirectories(libDir);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory " + libDir, e);
            }
        }
        Path deploaderFile = libDir.resolve("fplib_deploader.jar");
        if (Files.exists(deploaderFile)) {
            try {
                Files.delete(deploaderFile);
            } catch (IOException ignored) {
            }
        }
        try (OutputStream out = Files.newOutputStream(deploaderFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
             JarFile jarIn = new JarFile(scanner.path.toFile())) {
            JarEntry theEntry = jarIn.getJarEntry("fplib_deploader.jar");
            IOUtils.copy(jarIn.getInputStream(theEntry), out);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write deploader jar file!", e);
        }

        try {
            addURLToClassPath(deploaderFile.toUri().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to add deploader to classpath!", e);
        }
        try {
            Class<?> bootstrap = cl.loadClass("com_falsepattern_deploader_Bootstrap".replace("_", "."));
            Method bootstrapMethod = bootstrap.getDeclaredMethod("bootstrap", boolean.class, Path.class);
            bootstrapMethod.invoke(null, rfb, mcHomeDir);
            Method runDepLoaderMethod = bootstrap.getDeclaredMethod("runDepLoader");
            Launch.blackboard.put(BLACKBOARD_MARKER, bootstrap);
            Launch.blackboard.put(BLACKBOARD_RUN_DEPLOADER_MARKER, runDepLoaderMethod);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to load deploader bootstrap class!", e);
        }
    }

    private void scanCandidates(Path dir) {
        if (!Files.exists(dir) || !Files.isDirectory(dir))
            return;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            stream.forEach(this::scanCandidate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void scanCandidate(Path file) {
        if (Files.isRegularFile(file) && file.getFileName().toString().endsWith(".jar")) {
            try(JarFile jarFile = new JarFile(file.toFile())) {
                JarEntry entry = jarFile.getJarEntry("fplib_deploader.jar");
                if (entry != null) {
                    JarInputStream jarInput = new JarInputStream(jarFile.getInputStream(entry));
                    while (jarInput.getManifest() == null) {
                        jarInput.getNextJarEntry();
                    }
                    String version = jarInput.getManifest().getMainAttributes().getValue("FPLib-Deploader-Version");
                    if (version != null) {
                        int versionInt = Integer.parseInt(version);
                        if (versionInt > this.version) {
                            this.version = versionInt;
                            this.path = file;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public static void runDepLoader() {
        Object runDepLoader = Launch.blackboard.get(BLACKBOARD_RUN_DEPLOADER_MARKER);
        if (runDepLoader == null) {
            throw new IllegalStateException("Tried to call runDepLoader without calling bootstrap() first!");
        }
        try {
            ((Method)runDepLoader).invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("FalsePatternLib Deploader exited with an exception.", e);
        }
    }

    public static void addURLToClassPath(URL url) {
        if (rfbDetected) {
            RFBUtil.addURLToClassPath(url);
        } else {
            LaunchWrapperUtil.addURLToClassPath(url);
        }
    }

    private static URLClassLoader classLoader() {
        if (rfbDetected) {
            return RFBUtil.classLoader();
        } else {
            return LaunchWrapperUtil.classLoader();
        }
    }

    public static Path gameDir() {
        if (rfbDetected) {
            return RFBUtil.gameDir();
        } else {
            return LaunchWrapperUtil.gameDir();
        }
    }

    private static class RFBUtil {
        static void preinit() {
            ExtensibleClassLoader loader = RfbApiImpl.INSTANCE.launchClassLoader();
            try {
                Method exc = loader.getClass()
                                .getDeclaredMethod("addClassLoaderExclusion", String.class);
                exc.invoke(loader, "com_falsepattern_deploader_".replace("_", "."));
            } catch (Exception ignored) {}
        }

        static void addURLToClassPath(URL url) {
            RfbApiImpl.INSTANCE.compatClassLoader().addURL(url);
        }

        static URLClassLoader classLoader() {
            return RfbApiImpl.INSTANCE.compatClassLoader().asURLClassLoader();
        }

        static Path gameDir() {
            return RfbApiImpl.INSTANCE.gameDirectory();
        }
    }

    private static class LaunchWrapperUtil {
        static Path gameDir() {
            return Launch.minecraftHome == null ? Paths.get(".") : Launch.minecraftHome.toPath();
        }
        static void addURLToClassPath(URL url) {
            Launch.classLoader.addURL(url);
        }

        static URLClassLoader classLoader() {
            return Launch.classLoader;
        }
    }
}
