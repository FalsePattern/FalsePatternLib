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

package com.falsepattern.lib.internal;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Share {
    public static final Logger LOG = LogManager.getLogger(Tags.MODNAME);

    public static final boolean DEV_ENV;

    static {
        try {
            val bs = ((LaunchClassLoader) Share.class.getClassLoader()).getClassBytes("net.minecraft.world.World");
            DEV_ENV = bs != null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
