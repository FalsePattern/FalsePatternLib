/*
 * Copyright (C) 2022-2023 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
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

package com.falsepattern.lib.internal.asm;

import com.falsepattern.lib.internal.core.LowLevelCallMultiplexer;
import com.falsepattern.lib.internal.impl.dependencies.DependencyLoaderImpl;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbPlugin;

public class RFBLoadingPlugin implements RfbPlugin {
    static {
        LowLevelCallMultiplexer.rfbDetected();
        DependencyLoaderImpl.executeDependencyLoading(false);
    }
}
