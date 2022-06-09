package com.falsepattern.lib.updates;

import com.falsepattern.lib.StableAPI;
import lombok.Data;
import lombok.NonNull;
import org.apache.logging.log4j.Logger;

@Data
@StableAPI(since = "0.8.0")
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
