package com.falsepattern.lib.api;

import lombok.NonNull;
import lombok.val;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ComplexVersion extends Version {
    final Version[] versions;
    public ComplexVersion(@NonNull Version mainVersion, Version... subVersions) {
        this.versions = new Version[subVersions.length + 1];
        this.versions[0] = mainVersion;
        System.arraycopy(subVersions, 0, this.versions, 1, subVersions.length);
    }

    @Override
    public int compareTo(@NonNull Version o) {
        if (o instanceof ComplexVersion) {
            val other = (ComplexVersion) o;
            int count = Math.min(versions.length, other.versions.length);
            for (int i = 0; i < count; i++) {
                val result = versions[i].compareTo(other.versions[i]);
                if (result != 0) return result;
            }
            if (versions.length != other.versions.length) {
                return versions.length - other.versions.length;
            } else {
                return 0;
            }
        } else if (o instanceof SemanticVersion) {
            val other = (SemanticVersion) o;
            val result = other.compareTo(versions[0]);
            if (result != 0) {
                return result;
            }
            if (versions.length > 1) {
                return -1;
            }
            return 0;
        } else {
            throw new IllegalArgumentException("Could not compare version with class " + o.getClass().getName());
        }
    }

    @Override
    public String toString() {
        return Arrays.stream(versions).map(Version::toString).collect(Collectors.joining("-"));
    }
}
