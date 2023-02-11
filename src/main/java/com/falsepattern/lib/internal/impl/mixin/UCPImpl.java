/*
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lib.internal.impl.mixin;

import lombok.val;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.io.IOException;

public final class UCPImpl {
    private static final boolean GRIMOIRE;

    static {
        boolean grimoire = false;
        String[] knownGrimoireClassNames =
                new String[]{"io.github.crucible.grimoire.Grimoire", "io.github.crucible.grimoire.common.GrimoireCore"};
        for (val className : knownGrimoireClassNames) {
            try {
                if (Launch.classLoader.getClassBytes(className) != null) {
                    grimoire = true;
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        GRIMOIRE = grimoire;
    }

    public static void addJar(File pathToJar) throws Exception {
        if (!GRIMOIRE) {
            final LaunchClassLoader loader = Launch.classLoader;
            loader.addURL(pathToJar.toURI().toURL());
            // Act as-if we only added the mod to ucp
            loader.getSources().remove(loader.getSources().size() - 1);
        }
    }
}
