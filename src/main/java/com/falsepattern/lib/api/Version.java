package com.falsepattern.lib.api;

public abstract class Version implements Comparable<Version> {
    Version(){}

    public boolean equals(Version other) {
        return compareTo(other) == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Version)) return false;
        return equals((Version) obj);
    }
}
