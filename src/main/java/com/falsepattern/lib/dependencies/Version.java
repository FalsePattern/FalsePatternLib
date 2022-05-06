package com.falsepattern.lib.dependencies;

import com.falsepattern.lib.StableAPI;

@StableAPI(since = "0.6.0")
public abstract class Version implements Comparable<Version> {
    protected Version(){}

    public boolean equals(Version other) {
        return compareTo(other) == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Version)) return false;
        return equals((Version) obj);
    }
}
