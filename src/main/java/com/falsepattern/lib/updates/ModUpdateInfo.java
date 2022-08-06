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
import lombok.Data;
import lombok.NonNull;
import org.apache.logging.log4j.Logger;

@Data
@StableAPI(since = "0.8.0")
public class ModUpdateInfo {
    @NonNull
    @StableAPI.Expose
    public final String modID;
    @NonNull
    @StableAPI.Expose
    public final String currentVersion;
    @NonNull
    @StableAPI.Expose
    public final String latestVersion;
    @NonNull
    @StableAPI.Expose
    public final String updateURL;

    @StableAPI.Expose
    public void log(Logger logger) {
        logger.info("Updates are available for mod {}: Currently installed version is {}, " +
                    "latest available version is {}. Update URL: {}", modID, currentVersion, latestVersion,
                    updateURL.isEmpty() ? "unavailable" : updateURL);
    }
}
