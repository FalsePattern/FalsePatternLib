package com.falsepattern.lib.api;

final class Deprecation {
    private static boolean shortWarning = false;
    static void warn() {
        if (shortWarning) {
            new DeprecationWarning("FalsePatternLib api deprecation warning. The api will be removed in 0.7! Update your mod. Stacktrace:").printStackTrace();
        } else {
            new DeprecationWarning("\nDEPRECATION WARNING\nDEPRECATION WARNING\n" +
                                   "Someone used the deprecated FalsePatternLib api from 0.5 and before.\n" +
                                   "This api was deprecated in 0.6.0, and will be REMOVED in FalsePatternLib 0.7! You should probably update your mods.\n" +
                                   "See the following stacktrace for hints on what mod might be the cause.").printStackTrace();
            shortWarning = true;
        }
    }

    private static class DeprecationWarning extends Exception {
        public DeprecationWarning(String message) {
            super(message);
        }
    }
}
