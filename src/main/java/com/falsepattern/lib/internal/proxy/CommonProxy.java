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
package com.falsepattern.lib.internal.proxy;

import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.internal.impl.config.event.CommonEventHandlerPost;
import com.falsepattern.lib.internal.impl.config.event.CommonEventHandlerPre;
import com.falsepattern.lib.internal.impl.config.net.SyncPrompt;
import com.falsepattern.lib.internal.impl.config.net.SyncPromptHandler;
import com.falsepattern.lib.internal.impl.config.net.SyncReply;
import com.falsepattern.lib.internal.impl.config.net.SyncReplyHandler;
import com.falsepattern.lib.internal.impl.config.net.SyncRequest;
import com.falsepattern.lib.internal.impl.config.net.SyncRequestHandler;

import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;

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
