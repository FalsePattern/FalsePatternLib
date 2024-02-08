/*
 * Copyright (C) 2022-2023 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
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
package com.falsepattern.lib.internal.asm;

import com.falsepattern.lib.internal.Share;
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.impl.dependencies.DependencyLoaderImpl;
import com.falsepattern.lib.mapping.MappingManager;
import lombok.Getter;
import lombok.val;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;

import java.util.Map;

import static com.falsepattern.lib.mixin.MixinInfo.isClassPresentSafe;

/**
 * Coremod stub so that forge loads the jar early
 */
@MCVersion("1.7.10")
@Name(Tags.MODID)
@SortingIndex(500)
@IFMLLoadingPlugin.TransformerExclusions({Tags.GROUPNAME + ".internal.asm", Tags.GROUPNAME + ".asm"})
public class CoreLoadingPlugin implements IFMLLoadingPlugin {
    @Getter
    private static boolean obfuscated;

    static {
        Share.LOG.info("Removing skill issues...");
        try {
            Class.forName("thermos.Thermos");
            Share.LOG.fatal("Sorry, i prefer iced coffee.");
            throw skillIssue("Thermos is not supported by FalsePatternLib, please use a normal forge server.");
        } catch (ClassNotFoundException ignored) {
        }
        //Scan for dependencies now
        Share.LOG.info("Scanning for deps...");
        long start = System.nanoTime();
        DependencyLoaderImpl.scanDeps();
        long end = System.nanoTime();
        Share.LOG.info("Scanned in " + (end - start) / 1000000 + "ms");
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

    public static void validateGasStation() {
        Share.LOG.info("Got any gas?");
        //Make sure everything is loaded correctly, crash if gasstation is bugged
        // @formatter:off
        if (!isClassPresentSafe("com.falsepattern.gasstation.core.GasStationCore") //Validate core class
            || !isClassPresentSafe("makamys.mixingasm.api.TransformerInclusions") //Validate the mixingasm compat
            || !isClassPresentSafe("ru.timeconqueror.spongemixins.core.SpongeMixinsCore") //Validate the spongemixins compat
            || !isClassPresentSafe("io.github.tox1cozz.mixinbooterlegacy.MixinBooterLegacyPlugin") //Validate the MBL compat
            || !isClassPresentSafe("org.spongepowered.asm.lib.Opcodes") //Validate correct mixins class
            || isClassPresentSafe("org.spongepowered.libraries.org.objectweb.asm.Opcodes")
        ) {
            Share.LOG.fatal("Somebody put diesel in my gas tank!");
            throw new Error("Failed to validate your GasStation mixin plugin installation. "
                            + "Please make sure you have the latest GasStation installed from the official source: "
                            + "https://github.com/FalsePattern/GasStation");
        }
        // @formatter:on
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
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
