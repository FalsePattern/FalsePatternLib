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

package com.falsepattern.lib.internal.asm;

import com.falsepattern.lib.internal.core.LowLevelCallMultiplexer;
import com.falsepattern.lib.internal.impl.dependencies.DependencyLoaderImpl;
import com.falsepattern.lib.internal.impl.dependencies.LetsEncryptHelper;
import com.gtnewhorizons.retrofuturabootstrap.RfbApiImpl;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbPlugin;
import lombok.val;

import java.net.URLClassLoader;

public class RFBLoadingPlugin implements RfbPlugin {
    static {
        val loader = RfbApiImpl.INSTANCE.launchClassLoader();
        try {
            val exc = loader.getClass()
                            .getDeclaredMethod("addClassLoaderExclusion", String.class);
            exc.invoke(loader, "com.falsepattern.lib.dependencies.");
            exc.invoke(loader, "com.falsepattern.lib.internal.impl.dependencies.");
            exc.invoke(loader, "com.falsepattern.lib.internal.Internet");
            exc.invoke(loader, "com.falsepattern.lib.internal.Share");
            exc.invoke(loader, "com.falsepattern.lib.internal.asm.PreShare");
            exc.invoke(loader, "com.falsepattern.lib.internal.FPLog");
            exc.invoke(loader, "com.falsepattern.lib.internal.Tags");
            exc.invoke(loader, "com.falsepattern.lib.internal.config.EarlyConfig");
            exc.invoke(loader, "com.falsepattern.lib.internal.core.LowLevelCallMultiplexer");
        } catch (Exception ignored) {}
        PreShare.initDevState(((URLClassLoader)loader).findResource("net/minecraft/world/World.class") != null);
        PreShare.initClientState(((URLClassLoader)loader).findResource("net/minecraft/client/Minecraft.class") != null ||
                                 ((URLClassLoader)loader).findResource("bao.class") != null);
        LetsEncryptHelper.replaceSSLContext();
        LowLevelCallMultiplexer.rfbDetected();
        DependencyLoaderImpl.executeDependencyLoading();
    }
}
