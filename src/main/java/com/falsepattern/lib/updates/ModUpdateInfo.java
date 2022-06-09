package com.falsepattern.lib.updates;

import lombok.Data;

@Data
public class ModUpdateInfo {
    public final String modID;
    public final String currentVersion;
    public final String latestVersion;
    public final String updateURL;
}
