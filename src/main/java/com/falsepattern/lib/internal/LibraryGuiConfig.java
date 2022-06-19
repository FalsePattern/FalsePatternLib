package com.falsepattern.lib.internal;

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.SimpleGuiConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;

@SideOnly(Side.CLIENT)
public class LibraryGuiConfig extends SimpleGuiConfig {
    public LibraryGuiConfig(GuiScreen parent) throws ConfigException {
        super(parent, LibraryConfig.class, Tags.MODID, Tags.MODNAME);
    }
}
