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
package com.falsepattern.lib.internal.asm;

import com.falsepattern.deploader.DeploaderStub;
import com.falsepattern.lib.internal.FPLog;
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.logging.CrashImprover;
import com.falsepattern.lib.internal.logging.NotEnoughVerbosity;
import com.falsepattern.lib.mapping.MappingManager;
import com.falsepattern.lib.turboasm.MergeableTurboTransformer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;

import java.util.List;
import java.util.Map;

/**
 * Coremod stub so that forge loads the jar early
 */
@MCVersion("1.7.10")
@Name(Tags.MODID)
@SortingIndex(1100)
@IFMLLoadingPlugin.TransformerExclusions({Tags.GROUPNAME + ".internal.asm", Tags.GROUPNAME + ".asm", Tags.GROUPNAME + ".turboasm"})
public class CoreLoadingPlugin implements IFMLLoadingPlugin {
    @Getter
    private static boolean obfuscated;

    static {
        DeploaderStub.bootstrap(false);
        try {
            Class.forName("thermos.Thermos");
            loudWarning("Thermos is not officially supported by FalsePatternLib, or any of our other mods. " +
                        "Do not report any bugs to FalsePattern or MEGA from Thermos servers, unless " +
                        "you're also submitting a pull request to fix it. All non Thermos-specific errors " +
                        "MUST be reproducible in a non-Thermos environment to be considered valid.");
        } catch (ClassNotFoundException ignored) {
        }
        //Scan for dependencies now
        FPLog.LOG.info("Scanning for deps...");
        long start = System.nanoTime();
        DeploaderStub.runDepLoader();
        long end = System.nanoTime();
        FPLog.LOG.info("Scanned in " + (end - start) / 1000000 + "ms");
        //Initializing the rest
        MappingManager.initialize();
        NotEnoughVerbosity.apply();
        CrashImprover.probe();
    }

    private static void loudWarning(String message) {
        val len = message.length();
        val lines = new StringBuilder(4 + len);
        lines.append("--");
        for (int i = 0; i < len; i++) {
            lines.append('-');
        }
        lines.append("--");
        val l = lines.toString();
        for (int i = 0; i < 5; i++) {
            FPLog.LOG.fatal(l);
        }
        for (int i = 0; i < 5; i++) {
            FPLog.LOG.fatal("| {} |", message);
        }
        for (int i = 0; i < 5; i++) {
            FPLog.LOG.fatal(l);
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{Tags.GROUPNAME + ".internal.asm.FPTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        obfuscated = (Boolean) data.get("runtimeDeobfuscationEnabled");
        mergeTurboTransformers();
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @SneakyThrows
    public static synchronized void mergeTurboTransformers() {
        val f = LaunchClassLoader.class.getDeclaredField("transformers");
        f.setAccessible(true);

        @SuppressWarnings("unchecked")
        val transformers = (List<IClassTransformer>) f.get(Launch.classLoader);
        for (int i = 0; i < transformers.size() - 1; i++) {
            val a = transformers.get(i);
            if (!(a instanceof MergeableTurboTransformer))
                continue;

            val b = transformers.get(i + 1);
            if (!(b instanceof MergeableTurboTransformer))
                continue;

            transformers.remove(i + 1);
            transformers.set(i, MergeableTurboTransformer.merge((MergeableTurboTransformer) a, (MergeableTurboTransformer) b));
            i--;
        }
    }
}
