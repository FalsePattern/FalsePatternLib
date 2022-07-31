/**
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
import lombok.RequiredArgsConstructor;
import lombok.val;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This event is pushed on the FMLCommonHandler event bus once each time a single config synchronization has finished.
 *
 * This is a client-only event, and is never triggered serverside!
 */
@SideOnly(Side.CLIENT)
@StableAPI(since = "0.10.0")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ConfigSyncEvent extends Event {
    @StableAPI(since = "0.10.0")
    public final Class<?> configClass;

    public static boolean postStart(Class<?> configClass) {
        val event = new Start(configClass);
        FMLCommonHandler.instance().bus().post(event);
        return !event.isCanceled();
    }

    public static void postEndSuccess(Class<?> configClass) {
        FMLCommonHandler.instance().bus().post(new End(configClass, true, null));
    }

    public static void postEndFailure(Class<?> configClass, Throwable error) {
        FMLCommonHandler.instance().bus().post(new End(configClass, false, error));
    }

    @SideOnly(Side.CLIENT)
    @StableAPI(since = "0.10.0")
    public static class Start extends ConfigSyncEvent {
        private Start(Class<?> configClass) {
            super(configClass);
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    @SideOnly(Side.CLIENT)
    @StableAPI(since = "0.10.0")
    public static class End extends ConfigSyncEvent {
        @StableAPI(since = "0.10.0")
        public final boolean successful;
        /**
         * null if successful == true.
         */
        @StableAPI(since = "0.10.0")
        public final Throwable error;

        private End(Class<?> configClass, boolean successful, Throwable error) {
            super(configClass);
            this.successful = successful;
            this.error = error;
        }
    }
}
