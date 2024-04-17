/*
 * This file is part of FalsePatternLib.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalsePatternLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalsePatternLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalsePatternLib. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lib.internal.core;

import com.gtnewhorizons.retrofuturabootstrap.RfbApiImpl;
import lombok.experimental.UtilityClass;

import net.minecraft.launchwrapper.Launch;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@UtilityClass
public class LowLevelCallMultiplexer {
    private static boolean rfbDetected = false;
    public static void rfbDetected() {
        rfbDetected = true;
    }

    public static void addURLToClassPath(URL url) {
        if (rfbDetected) {
            RFBLowLevel.addURLToClassPath(url);
        } else {
            LaunchWrapperLowLevel.addURLToClassPath(url);
        }
    }

    public static List<URL> getClassPathSources() {
        if (rfbDetected) {
            return RFBLowLevel.getClassPathSources();
        } else {
            return LaunchWrapperLowLevel.getClassPathSources();
        }
    }

    public static Path gameDir() {
        if (rfbDetected) {
            return RFBLowLevel.gameDir();
        } else {
            return LaunchWrapperLowLevel.gameDir();
        }
    }

    //Separate classes to avoid accidental classloading

    @SuppressWarnings("resource")
    private static class RFBLowLevel {
        static void addURLToClassPath(URL url) {
            RfbApiImpl.INSTANCE.compatClassLoader().addURL(url);
        }

        static List<URL> getClassPathSources() {
            return RfbApiImpl.INSTANCE.compatClassLoader().getSources();
        }

        static Path gameDir() {
            return RfbApiImpl.INSTANCE.gameDirectory();
        }
    }

    private static class LaunchWrapperLowLevel {
        static void addURLToClassPath(URL url) {
            Launch.classLoader.addURL(url);
        }

        static List<URL> getClassPathSources() {
            return Launch.classLoader.getSources();
        }

        static Path gameDir() {
            return Launch.minecraftHome == null ? Paths.get(".") : Launch.minecraftHome.toPath();
        }
    }
}
