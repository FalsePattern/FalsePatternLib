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
package com.falsepattern.lib.internal.proxy;

import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.internal.Share;
import com.falsepattern.lib.internal.config.LibraryConfig;
import com.falsepattern.lib.internal.impl.config.event.CommonEventHandlerPost;
import com.falsepattern.lib.internal.impl.config.event.CommonEventHandlerPre;
import com.falsepattern.lib.internal.impl.config.net.SyncPrompt;
import com.falsepattern.lib.internal.impl.config.net.SyncPromptHandler;
import com.falsepattern.lib.internal.impl.config.net.SyncReply;
import com.falsepattern.lib.internal.impl.config.net.SyncReplyHandler;
import com.falsepattern.lib.internal.impl.config.net.SyncRequest;
import com.falsepattern.lib.internal.impl.config.net.SyncRequestHandler;
import com.falsepattern.lib.updates.UpdateChecker;
import lombok.val;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;

import java.util.Collections;

public class CommonProxy {
    public void construct(FMLConstructionEvent e) {
        FalsePatternLib.NETWORK.registerMessage(SyncRequestHandler.class, SyncRequest.class, 0, Side.SERVER);
        FalsePatternLib.NETWORK.registerMessage(SyncReplyHandler.class, SyncReply.class, 1, Side.CLIENT);
        FalsePatternLib.NETWORK.registerMessage(SyncPromptHandler.class, SyncPrompt.class, 2, Side.CLIENT);
    }

    public void preInit(FMLPreInitializationEvent e) {
        CommonEventHandlerPre.registerBus();
    }

    public void init(FMLInitializationEvent e) {
    }

    public void postInit(FMLPostInitializationEvent e) {
        CommonEventHandlerPost.registerBus();
    }
}
