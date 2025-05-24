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

package com.falsepattern.lib.internal.logging;

import com.falsepattern.lib.internal.FPLog;
import lombok.val;

import net.minecraft.launchwrapper.Launch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CrashImprover {
    private static final String cookie = UUID.randomUUID().toString();
    static {
        FPLog.LOG.info("Magic cookie: {}", cookie);
    }
    public static void probe() {

    }

    public static void injectLatest(FileWriter writer) {
        val potentialLogs = Arrays.asList(new File(Launch.minecraftHome, "logs/fml-client-latest.log"),
                                          new File(Launch.minecraftHome, "logs/fml-server-latest.log"));
        for (val file: potentialLogs) {
            try(InputStream is = new FileInputStream(file)) {
                List<String> lines = readLines(is);
                if(lines.stream().anyMatch(l -> l.contains(cookie))) {
                    writer.append("\n\n").append("Complete log:").append('\n');
                    for (String line : lines) {
                        writer.append(line).append('\n');
                    }
                }
            } catch (IOException e) {
            }
        }
    }
    private static List<String> readLines(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.toList());
    }
}
