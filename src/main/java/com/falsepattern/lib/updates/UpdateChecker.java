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
package com.falsepattern.lib.updates;

import com.falsepattern.lib.DeprecationDetails;
import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.Share;
import com.falsepattern.lib.internal.impl.updates.UpdateCheckerImpl;

import net.minecraft.util.IChatComponent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@StableAPI(since = "0.8.0")
public final class UpdateChecker {
    /**
     * Checks for updates. The URL should be a JSON file that contains a list of mods, each with a mod ID, and one or more
     * versions. The JSON file must have the following format:
     * <pre>{@code
     *  [
     *      {
     *          "modid": "modid",
     *          "versions": [
     *              {
     *                  ...version object...
     *              },
     *              {
     *                  ...version object...
     *              }
     *          ]
     *      },
     *      {
     *          "modid": "modid2",
     *          "versions": {
     *              ...version object...
     *          },
     *          "updateURL": "https://example.com/mods/mymod2"
     *      },
     *      ...etc, one json object per mod.
     *  ]
     * }</pre>
     * The currently supported version object formats are:
     * <p>
     * Raw version entry:
     * <pre>{@code
     * {
     *     "type": "raw",
     *     "version": "1.0.0",
     *     "updateURL": "https://example.com/mods/mymod"
     * }
     * }</pre>
     * <p>
     * GitHub version entry:
     * <pre>{@code
     * {
     *    "type": "github",
     *    "repo": "Example/ExampleMod"
     * }
     * }</pre>
     * <p>
     * The GitHub version entry will automatically fetch the latest release from the specified repository using the GitHub API.
     *
     * @param url The URL to check
     *
     * @return A list of mods that were both available on the URL and installed
     */
    @StableAPI.Expose(since = "0.11.0")
    public static CompletableFuture<List<ModUpdateInfo>> fetchUpdatesAsyncV2(String url) {
        return UpdateCheckerImpl.fetchUpdatesAsyncV2(url);
    }

    /**
     * Same this as {@link #fetchUpdatesAsyncV2(String)}, but returns the result in a blocking fashion.
     *
     * @param url The URL to check
     *
     * @return A future that will contain the update info about mods that were both available on the URL and installed
     *
     * @throws UpdateCheckException If the update checker is disabled in config, the URL is invalid, or
     */
    @StableAPI.Expose(since = "0.11.0")
    public static List<ModUpdateInfo> fetchUpdatesV2(String url) throws UpdateCheckException {
        return UpdateCheckerImpl.fetchUpdatesV2(url);
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

    //region deprecated

    /**
     * DEPRECATED: Use the V2 API instead. This is kept for backwards compatibility.
     */
    @Deprecated
    @DeprecationDetails(deprecatedSince = "0.11.0",
                        replacement = "fetchUpdatesV2")
    @DeprecationDetails.RemovedInVersion("0.13")
    @StableAPI.Expose
    public static List<ModUpdateInfo> fetchUpdates(String url) throws UpdateCheckException {
        Share.deprecatedWarning(new Throwable());
        return UpdateCheckerImpl.fetchUpdates(url);
    }

    /**
     * DEPRECATED: Use the V2 API instead. This is kept for backwards compatibility.
     */
    @StableAPI.Expose
    @Deprecated
    @DeprecationDetails(deprecatedSince = "0.11.0",
                        replacement = "fetchUpdatesAsyncV2")
    @DeprecationDetails.RemovedInVersion("0.13")
    public static CompletableFuture<List<ModUpdateInfo>> fetchUpdatesAsync(String url) {
        Share.deprecatedWarning(new Throwable());
        return UpdateCheckerImpl.fetchUpdatesAsync(url);
    }
    //endregion
}
