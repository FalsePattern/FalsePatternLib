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
package com.falsepattern.lib.config.event;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.EventUtil;
import lombok.val;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This event is pushed on the FMLCommonHandler event bus once all the config synchronizations have finished.
 * <p>
 * This is a client-only event, and is never triggered serverside!
 */
@SideOnly(Side.CLIENT)
@StableAPI(since = "0.10.0")
public class AllConfigSyncEvent extends Event {

    @StableAPI.Internal
    public AllConfigSyncEvent() {
    }

    @StableAPI.Internal
    public static boolean postStart() {
        val event = new Start();
        return EventUtil.postOnCommonBus(event);
    }

    @StableAPI.Internal
    public static void postEnd() {
        EventUtil.postOnCommonBus(new End());
    }

    @SideOnly(Side.CLIENT)
    @StableAPI(since = "0.10.0")
    public static final class Start extends AllConfigSyncEvent {
        @StableAPI.Internal
        public Start() {
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    @SideOnly(Side.CLIENT)
    @StableAPI(since = "0.10.0")
    public static final class End extends AllConfigSyncEvent {
        @StableAPI.Internal
        public End() {
        }
    }
}
