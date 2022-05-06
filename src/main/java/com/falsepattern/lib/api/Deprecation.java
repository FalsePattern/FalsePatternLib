package com.falsepattern.lib.api;

final class Deprecation {
    static void warn() {
        new Exception("\nDEPRECATION WARNING\nDEPRECATION WARNING\n" +
                      "Someone used the deprecated FalsePatternLib api.\n" +
                      "This api will be replaced in FalsePatternLib 0.7! You should probably update your mods.\n" +
                      "See the following stacktrace for hints on what mod could be the cause.").printStackTrace();
    }
}
