package com.falsepattern.lib.dependencies;

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
