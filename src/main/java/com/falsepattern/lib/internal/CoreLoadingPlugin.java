package com.falsepattern.lib.internal;

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
    static {
        try {
            Class.forName("thermos.Thermos");
            val iAmNotFixingThisDontEvenAskPlusSkillIssue =
                    new Error("\n\n" +
                              "--------------------------------------------------------------------------------\n" +
                              "|Thermos is not supported by FalsePatternLib, please use a normal forge server.|\n" +
                              "|       Any bug reports concerning this message will be silently deleted.      |\n" +
                              "--------------------------------------------------------------------------------\n");
            iAmNotFixingThisDontEvenAskPlusSkillIssue.setStackTrace(new StackTraceElement[0]);
            throw iAmNotFixingThisDontEvenAskPlusSkillIssue;
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
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
