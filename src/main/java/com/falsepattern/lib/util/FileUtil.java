package com.falsepattern.lib.util;

import com.falsepattern.lib.StableAPI;
import net.minecraft.launchwrapper.Launch;

import java.io.File;

@StableAPI(since = "0.8.2")
public class FileUtil {
    public static File getMinecraftHome() {
        return Launch.minecraftHome == null ? new File(".") : Launch.minecraftHome;
    }
}
