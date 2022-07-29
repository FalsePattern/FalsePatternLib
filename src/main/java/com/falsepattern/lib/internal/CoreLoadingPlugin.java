package com.falsepattern.lib.internal;

import lombok.Getter;
import lombok.val;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import java.util.Map;

/**
 * Coremod stub so that forge loads the jar early
 */
@MCVersion("1.7.10")
@Name(Tags.MODID)
@SortingIndex(500)
public class CoreLoadingPlugin implements IFMLLoadingPlugin {
    @Getter
    private static boolean obfuscated;

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
    static {
        try {
            Class.forName("thermos.Thermos");
            throw skillIssue("Thermos is not supported by FalsePatternLib, please use a normal forge server.");
        } catch (ClassNotFoundException ignored) {}

    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
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
