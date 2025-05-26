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
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * You can use this event to manually trigger a synchronization request. The two different flavors are used on the two
 * different game sides. If you want to trigger this on the server, use {@link #postServer}, and if on the client, use
 * {@link #postClient}
 */
public class ConfigSyncRequestEvent extends Event {
    @ApiStatus.Internal
    public ConfigSyncRequestEvent() {
    }

    public static void postClient() {
        EventUtil.postOnCommonBus(new Client());
    }

    public static void postServer(List<EntityPlayerMP> players) {
        EventUtil.postOnCommonBus(new Server(new ArrayList<>(players)));
    }

    public static final class Client extends ConfigSyncRequestEvent {
        @ApiStatus.Internal
        public Client() {
        }
    }

    public static final class Server extends ConfigSyncRequestEvent {
        private final List<EntityPlayerMP> players;

        @ApiStatus.Internal
        public Server(List<EntityPlayerMP> players) {
            this.players = players;
        }

        public List<EntityPlayerMP> getPlayers() {
            return Collections.unmodifiableList(players);
        }
    }
}
