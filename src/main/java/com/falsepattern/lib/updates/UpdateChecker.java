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

import net.minecraft.util.IChatComponent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Obsolete, marked for removal.
 */
@StableAPI(since = "0.8.0")
@DeprecationDetails(deprecatedSince = "1.0.0",
                    replacement = "None, the update checker system is being removed.")
@DeprecationDetails.RemovedInVersion("1.1.0")
@Deprecated
public final class UpdateChecker {
    /**
     * Obsolete, marked for removal.
     */
    @StableAPI.Expose(since = "0.11.0")
    public static CompletableFuture<List<ModUpdateInfo>> fetchUpdatesAsyncV2(String url) {
        Share.deprecatedWarning(new Throwable());
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    /**
     * Obsolete, marked for removal.
     */
    @StableAPI.Expose(since = "0.11.0")
    public static List<ModUpdateInfo> fetchUpdatesV2(String url) throws UpdateCheckException {
        Share.deprecatedWarning(new Throwable());
        return Collections.emptyList();
    }

    /**
     * Obsolete, marked for removal.
     */
    @StableAPI.Expose
    public static List<IChatComponent> updateListToChatMessages(String initiator, List<ModUpdateInfo> updates) {
        Share.deprecatedWarning(new Throwable());
        return Collections.emptyList();
    }
}
