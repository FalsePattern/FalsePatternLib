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

import com.falsepattern.lib.internal.EventUtil;
import lombok.val;
import org.jetbrains.annotations.ApiStatus;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This event is pushed on the FMLCommonHandler event bus once each time a single config synchronization has finished.
 * <p>
 * This is a client-only event, and is never triggered serverside!
 */
@SideOnly(Side.CLIENT)
public class ConfigSyncEvent extends Event {
    public final Class<?> configClass;

    @ApiStatus.Internal
    public ConfigSyncEvent(Class<?> configClass) {
        this.configClass = configClass;
    }

    @ApiStatus.Internal
    public static boolean postStart(Class<?> configClass) {
        val event = new Start(configClass);
        return EventUtil.postOnCommonBus(event);
    }

    @ApiStatus.Internal
    public static void postEndSuccess(Class<?> configClass) {
        EventUtil.postOnCommonBus(new End(configClass, true, null));
    }

    @ApiStatus.Internal
    public static void postEndFailure(Class<?> configClass, Throwable error) {
        EventUtil.postOnCommonBus(new End(configClass, false, error));
    }

    @SideOnly(Side.CLIENT)
    public static final class Start extends ConfigSyncEvent {

        @ApiStatus.Internal
        public Start(Class<?> configClass) {
            super(configClass);
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    @SideOnly(Side.CLIENT)
    public static final class End extends ConfigSyncEvent {
        public final boolean successful;
        public final Throwable error;

        @ApiStatus.Internal
        public End(Class<?> configClass, boolean successful, Throwable error) {
            super(configClass);
            this.successful = successful;
            this.error = error;
        }
    }
}
