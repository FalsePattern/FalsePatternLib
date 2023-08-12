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
package com.falsepattern.lib.config.event;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.EventUtil;
import lombok.val;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This event is pushed on the FMLCommonHandler event bus once each time a single config synchronization has finished.
 * <p>
 * This is a client-only event, and is never triggered serverside!
 */
@SideOnly(Side.CLIENT)
@StableAPI(since = "0.10.0")
public class ConfigSyncEvent extends Event {
    @StableAPI.Expose
    public final Class<?> configClass;

    @StableAPI.Internal
    public ConfigSyncEvent(Class<?> configClass) {
        this.configClass = configClass;
    }

    @StableAPI.Internal
    public static boolean postStart(Class<?> configClass) {
        val event = new Start(configClass);
        return EventUtil.postOnCommonBus(event);
    }

    @StableAPI.Internal
    public static void postEndSuccess(Class<?> configClass) {
        EventUtil.postOnCommonBus(new End(configClass, true, null));
    }

    @StableAPI.Internal
    public static void postEndFailure(Class<?> configClass, Throwable error) {
        EventUtil.postOnCommonBus(new End(configClass, false, error));
    }

    @SideOnly(Side.CLIENT)
    @StableAPI(since = "0.10.0")
    public static final class Start extends ConfigSyncEvent {

        @StableAPI.Internal
        public Start(Class<?> configClass) {
            super(configClass);
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    @SideOnly(Side.CLIENT)
    @StableAPI(since = "0.10.0")
    public static final class End extends ConfigSyncEvent {
        @StableAPI.Expose
        public final boolean successful;

        @StableAPI.Expose
        public final Throwable error;

        @StableAPI.Internal
        public End(Class<?> configClass, boolean successful, Throwable error) {
            super(configClass);
            this.successful = successful;
            this.error = error;
        }
    }
}
