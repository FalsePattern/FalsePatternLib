/*
 * This file is part of FalsePatternLib.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalsePatternLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * FalsePatternLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalsePatternLib. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lib.internal.impl.dependencies;

import com.falsepattern.lib.dependencies.ComplexVersion;
import com.falsepattern.lib.dependencies.Library;
import com.falsepattern.lib.dependencies.SemanticVersion;
import com.falsepattern.lib.dependencies.Version;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DependencyLoaderImpl {

    public static void addMavenRepo(String url) {
        com.falsepattern.deploader.DependencyLoaderImpl.addMavenRepo(url);
    }

    /**
     * @since 0.10.0
     */
    public static CompletableFuture<Void> loadLibrariesAsync(Library... libraries) {
        val libs = new com.falsepattern.deploader.Library[libraries.length];
        for (int i = 0; i < libraries.length; i++) {
            libs[i] = adaptLibrary(libraries[i]);
        }
        return com.falsepattern.deploader.DependencyLoaderImpl.loadLibrariesAsync(libs);
    }

    /**
     * @since 0.10.0
     */
    public static void loadLibraries(Library... libraries) {
        val libs = new com.falsepattern.deploader.Library[libraries.length];
        for (int i = 0; i < libraries.length; i++) {
            libs[i] = adaptLibrary(libraries[i]);
        }
        com.falsepattern.deploader.DependencyLoaderImpl.loadLibraries(libs);
    }

    public static Version parseVersion(String versionString) {
        val realVersion = com.falsepattern.deploader.DependencyLoaderImpl.parseVersion(versionString);
        return adaptVersion(realVersion);
    }

    private static @Nullable Version adaptVersion(@Nullable com.falsepattern.deploader.version.Version version) {
        if (version == null)
            return null;

        if (version instanceof com.falsepattern.deploader.version.RawVersion raw) {
            return new RawVersion(raw.versionString);
        }
        if (version instanceof com.falsepattern.deploader.version.SemanticVersion sem) {
            return new SemanticVersion(sem.getMajorVersion(), sem.getMinorVersion(), sem.getPatchVersion(), sem.getPreRelease(), sem.getBuild());
        }
        if (version instanceof com.falsepattern.deploader.version.ComplexVersion cplx) {
            val versA = cplx.versions;
            val vers = new Version[versA.length];
            for (int i = 0; i < versA.length; i++) {
                vers[i] = adaptVersion(versA[i]);
            }
            return new ComplexVersion(vers);
        }
        throw new IllegalStateException("Unknown version class " + version.getClass().getName());
    }

    @Contract("null -> null;!null -> !null")
    private static @Nullable com.falsepattern.deploader.version.Version adaptVersion(@Nullable Version version) {
        if (version == null)
            return null;
        if (version instanceof RawVersion raw) {
            return new com.falsepattern.deploader.version.RawVersion(raw.versionString);
        }
        if (version instanceof SemanticVersion sem) {
            return new com.falsepattern.deploader.version.SemanticVersion(sem.getMajorVersion(), sem.getMinorVersion(), sem.getPatchVersion(), sem.getPreRelease(), sem.getBuild());
        }
        if (version instanceof ComplexVersion cplx) {
            val versA = cplx.versionsRaw();
            val vers = new com.falsepattern.deploader.version.Version[versA.length];
            for (int i = 0; i < versA.length; i++) {
                vers[i] = adaptVersion(versA[i]);
            }
            return new com.falsepattern.deploader.version.ComplexVersion(vers);
        }
        throw new IllegalStateException("Unknown version class " + version.getClass().getName());
    }

    private static com.falsepattern.deploader.Library adaptLibrary(Library library) {
        return new com.falsepattern.deploader.Library(
                library.loadingModId,
                library.groupId,
                library.artifactId,
                adaptVersion(library.minVersion),
                adaptVersion(library.maxVersion),
                adaptVersion(library.preferredVersion),
                library.regularSuffix,
                library.devSuffix
        );
    }
}
