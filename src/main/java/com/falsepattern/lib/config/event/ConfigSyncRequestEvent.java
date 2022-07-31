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

import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * You can use this event to manually trigger a synchronization request. The two different flavors are used on the two
 * different game sides. If you want to trigger this on the server, use {@link #postServer}, and if on the client, use
 * {@link #postClient}
 *
 */
@StableAPI(since = "0.10.0")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ConfigSyncRequestEvent extends Event {
    @StableAPI(since = "0.10.0")
    public static class Client extends ConfigSyncRequestEvent {

    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @StableAPI(since = "0.10.0")
    public static class Server extends ConfigSyncRequestEvent {
        private final List<EntityPlayerMP> players;

        public List<EntityPlayerMP> getPlayers() {
            return Collections.unmodifiableList(players);
        }
    }

    @StableAPI(since = "0.10.0")
    public static void postClient() {
        FMLCommonHandler.instance().bus().post(new Client());
    }

    @StableAPI(since = "0.10.0")
    public static void postServer(List<EntityPlayerMP> players) {
        FMLCommonHandler.instance().bus().post(new Server(new ArrayList<>(players)));
    }
}
