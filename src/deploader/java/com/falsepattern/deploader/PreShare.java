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

package com.falsepattern.deploader;

public class PreShare {
    private static volatile int devEnv = -1;
    private static volatile int client = -1;
    public static synchronized void initDevState(boolean state) {
        if (devEnv >= 0) {
            return;
        }
        devEnv = state ? 1 : 0;
    }
    public static synchronized void initClientState(boolean state) {
        if (client >= 0) {
            return;
        }
        client = state ? 1 : 0;
    }

    public static synchronized boolean devEnv() {
        return devEnv == 1;
    }

    public static synchronized boolean client() {
        return client == 1;
    }
}
