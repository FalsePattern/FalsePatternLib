package com.falsepattern.lib.api;

final class Deprecation {
    static void warn() {
        new Exception("\nDEPRECATION WARNING\nDEPRECATION WARNING\n" +
                      "Someone used the deprecated FalsePatternLib api from 0.5 and before.\n" +
                      "This api was deprecated in 0.6.0, and will be REMOVED in FalsePatternLib 0.7! You should probably update your mods.\n" +
                      "See the following stacktrace for hints on what mod might be the cause.").printStackTrace();
    }
}
