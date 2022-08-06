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
package com.falsepattern.lib.config.event;

import com.falsepattern.lib.StableAPI;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import cpw.mods.fml.common.FMLCommonHandler;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED,
                   onConstructor_ = @StableAPI.Internal)
public class AllConfigSyncEvent extends Event {

    @StableAPI.Internal
    public static boolean postStart() {
        val event = new Start();
        FMLCommonHandler.instance().bus().post(event);
        return !event.isCanceled();
    }

    @StableAPI.Internal
    public static void postEnd() {
        FMLCommonHandler.instance().bus().post(new End());
    }

    @SideOnly(Side.CLIENT)
    @StableAPI(since = "0.10.0")
    @NoArgsConstructor(access = AccessLevel.PUBLIC,
                       onConstructor_ = @StableAPI.Internal)
    public static final class Start extends AllConfigSyncEvent {
        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    @SideOnly(Side.CLIENT)
    @StableAPI(since = "0.10.0")
    @NoArgsConstructor(access = AccessLevel.PUBLIC,
                       onConstructor_ = @StableAPI.Internal)
    public static final class End extends AllConfigSyncEvent {
    }
}
