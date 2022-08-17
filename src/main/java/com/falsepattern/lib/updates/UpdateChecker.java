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
package com.falsepattern.lib.updates;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.impl.updates.UpdateCheckerImpl;

import net.minecraft.util.IChatComponent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@StableAPI(since = "0.8.0")
public final class UpdateChecker {
    /**
     * Checks for updates. The URL should be a JSON file that contains a list of mods, each with a mod ID, one or more
     * versions, and a URL for the user to check for updates in case the current and latest versions are different.
     * The JSON file must have the following format:
     * <pre>{@code
     *  [
     *      {
     *          "modID": "modid",
     *          "latestVersion": ["1.0.0", "1.0.0-foo"],
     *          "updateURL": "https://example.com/mods/mymod"
     *      },
     *      {
     *          "modID": "modid2",
     *          "latestVersion": ["0.2.0", "0.3.0-alpha"],
     *          "updateURL": "https://example.com/mods/mymod2"
     *      },
     *      ...etc, one json object per mod.
     *  ]
     * }</pre>
     *
     * @param url The URL to check
     *
     * @return A list of mods that were both available on the URL and installed
     */
    @StableAPI.Expose
    public static CompletableFuture<List<ModUpdateInfo>> fetchUpdatesAsync(String url) {
        return UpdateCheckerImpl.fetchUpdatesAsync(url);
    }

    /**
     * Same this as {@link #fetchUpdatesAsync(String)}, but returns the result in a blocking fashion.
     *
     * @param url The URL to check
     *
     * @return A future that will contain the update info about mods that were both available on the URL and installed
     *
     * @throws UpdateCheckException If the update checker is disabled in config, the URL is invalid, or
     */
    @StableAPI.Expose
    public static List<ModUpdateInfo> fetchUpdates(String url) throws UpdateCheckException {
        return UpdateCheckerImpl.fetchUpdates(url);
    }

    /**
     * Formats the raw list of updates into lines of chat messages you can send to players.
     *
     * @param initiator Who/what/which mod did this update check
     * @param updates   The list of updates to convert
     *
     * @return A list of chat messages that can be sent to players
     */
    @StableAPI.Expose
    public static List<IChatComponent> updateListToChatMessages(String initiator, List<ModUpdateInfo> updates) {
        return UpdateCheckerImpl.updateListToChatMessages(initiator, updates);
    }
}
