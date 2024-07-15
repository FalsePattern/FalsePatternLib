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
package com.falsepattern.lib.internal.asm;

import com.falsepattern.lib.internal.FPLog;
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.impl.dependencies.DependencyLoaderImpl;
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

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Map;

import static com.falsepattern.lib.mixin.MixinInfo.isClassPresentSafe;

/**
 * Coremod stub so that forge loads the jar early
 */
@MCVersion("1.7.10")
@Name(Tags.MODID)
@SortingIndex(500)
@IFMLLoadingPlugin.TransformerExclusions({Tags.GROUPNAME + ".internal.asm", Tags.GROUPNAME + ".asm", Tags.GROUPNAME + ".turboasm"})
public class CoreLoadingPlugin implements IFMLLoadingPlugin {
    @Getter
    private static boolean obfuscated;

    static {
        FPLog.LOG.info("Removing skill issues...");
        try {
            Class.forName("thermos.Thermos");
            FPLog.LOG.fatal("Sorry, i prefer iced coffee.");
            throw skillIssue("Thermos is not supported by FalsePatternLib, please use a normal forge server.");
        } catch (ClassNotFoundException ignored) {
        }
        //Scan for dependencies now
        FPLog.LOG.info("Scanning for deps...");
        long start = System.nanoTime();
        DependencyLoaderImpl.executeDependencyLoading(true);
        long end = System.nanoTime();
        FPLog.LOG.info("Scanned in " + (end - start) / 1000000 + "ms");
        //Initializing the rest
        MappingManager.initialize();
    }

    @SuppressWarnings("SameParameterValue")
    private static Error skillIssue(String message) {
        int width = message.length();
        String shutup = "Any bug reports concerning this message will be silently deleted.";
        int suWidth = shutup.length();
        int padding = width - suWidth;
        boolean padMSG = padding < 0;
        if (padMSG) {
            padding = -padding;
        }
        int padLeft = padding / 2;
        int padRight = padding - padLeft;
        int maxWidth = Math.max(width, suWidth);
        StringBuilder bld = new StringBuilder("\n\n");
        for (int i = 0; i < maxWidth + 2; i++) {
            bld.append('-');
        }
        bld.append("\n|");
        if (padMSG) {
            for (int i = 0; i < padLeft; i++) {
                bld.append(' ');
            }
            bld.append(message);
            for (int i = 0; i < padRight; i++) {
                bld.append(' ');
            }
        } else {
            bld.append(message);
        }
        bld.append("|\n|");
        if (!padMSG) {
            for (int i = 0; i < padLeft; i++) {
                bld.append(' ');
            }
            bld.append(shutup);
            for (int i = 0; i < padRight; i++) {
                bld.append(' ');
            }
        } else {
            bld.append(shutup);
        }
        bld.append("|\n");
        for (int i = 0; i < maxWidth + 2; i++) {
            bld.append('-');
        }
        val skillIssue = new Error(bld.toString());
        skillIssue.setStackTrace(new StackTraceElement[0]);
        return skillIssue;
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
