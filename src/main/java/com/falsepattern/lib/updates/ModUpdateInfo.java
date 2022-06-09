package com.falsepattern.lib.updates;

import lombok.Data;
import lombok.NonNull;
import org.apache.logging.log4j.Logger;

@Data
public class ModUpdateInfo {
    @NonNull public final String modID;
    @NonNull public final String currentVersion;
    @NonNull public final String latestVersion;
    @NonNull public final String updateURL;

    public void log(Logger logger) {
        logger.info("Updates are available for mod {}: Currently installed version is {}, " +
                    "latest available version is {}. Update URL: {}",
                modID,
                currentVersion,
                latestVersion,
                updateURL.isEmpty() ? "unavailable": updateURL);
    }
}
