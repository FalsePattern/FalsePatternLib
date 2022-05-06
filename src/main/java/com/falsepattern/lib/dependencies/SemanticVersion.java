package com.falsepattern.lib.dependencies;

import com.falsepattern.lib.StableAPI;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import java.util.Objects;

@StableAPI(since = "0.6.0")
public class SemanticVersion extends Version {
    @Getter
    private final int majorVersion;
    @Getter
    private final int minorVersion;
    @Getter
    private final int patchVersion;
    @Getter
    private final String preRelease;
    @Getter
    private final String build;

    @Builder
    public SemanticVersion(int majorVersion, int minorVersion, int patchVersion, String preRelease, String build) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
        preRelease = preRelease == null ? null : preRelease.trim();
        build = build == null ? null : build.trim();
        this.preRelease = "".equals(preRelease) ? null : preRelease;
        this.build = "".equals(build) ? null : build;
    }

    public SemanticVersion(int majorVersion, int minorVersion, int patchVersion, String preRelease) {
        this(majorVersion, minorVersion, patchVersion, preRelease, null);
    }

    public SemanticVersion(int majorVersion, int minorVersion, int patchVersion) {
        this(majorVersion, minorVersion, patchVersion, null, null);
    }

    @Override
    public int compareTo(@NonNull Version o) {
        if (o instanceof ComplexVersion) {
            val result = this.compareTo(((ComplexVersion)o).versions[0]);
            if (result != 0) {
                return result;
            } else if (((ComplexVersion) o).versions.length > 1) {
                return 1;
            } else {
                return 0;
            }
        } else if (o instanceof SemanticVersion) {
            val other = (SemanticVersion)o;
            if (majorVersion != other.majorVersion) {
                return majorVersion - other.majorVersion;
            } else if (minorVersion != other.minorVersion) {
                return minorVersion - other.minorVersion;
            } else if (patchVersion != other.patchVersion) {
                return patchVersion - other.patchVersion;
            } else if (!Objects.equals(preRelease, other.preRelease)) {
                if (preRelease == null) {
                    return 1;
                } else if (other.preRelease == null) {
                    return -1;
                }
                return preRelease.compareTo(other.preRelease);
            } else {
                return 0;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return majorVersion + "." + minorVersion + "." + patchVersion + (preRelease == null ? "" : "-" + preRelease) + (build == null ? "" : "+" + build);
    }
}
