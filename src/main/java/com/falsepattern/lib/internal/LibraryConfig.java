package com.falsepattern.lib.internal;

import com.falsepattern.lib.config.Config;

@Config(modid = Tags.MODID)
public class LibraryConfig {
    @Config.Comment({"Used to control whether FalsePatternLib should check for outdated mods.",
                    "If you're building a public modpack, you should turn this off so that your users don't " +
                    "get nagged about outdated mods."})
    @Config.LangKey("config.falsepatternlib.updatecheck")
    public static boolean ENABLE_UPDATE_CHECKER = true;

    @Config.Comment({"Used to control whether FalsePatternLib should be allowed to use the internet.",
                    "If this is enabled, library downloads will be blocked.",
                    "Note that if a mod tries to download a library that is not downloaded yet, the game will crash."})
    @Config.LangKey("config.falsepatternlib.disableinternet")
    public static boolean ENABLE_LIBRARY_DOWNLOADS = true;
}
