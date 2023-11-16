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

/**
 * This package used to contain a system for checking for updates to mods.
 * This system is being removed, and this package is being kept for backwards compatibility.
 * Reasons for removal:
 * - In older versions of the mod, this used to show a message in chat when an update was available. This was annoying and
 *  not very useful, so it was removed.
 * - In 0.12, the updates were shown only in the console, which is not very visible, so people didn't notice them.
 * - The update checker system was very complicated and hard to maintain, and required externally hosted JSON files.
 * <p>
 * For anyone who wants to implement their own update checker, don't. Nobody cares about mod updates anyway,
 * they will keep playing on ancient versions of the mod no matter what you do or how many critical bugs you fix.
 */
@DeprecationDetails(deprecatedSince = "1.0.0",
                    replacement = "None, the update checker system is being removed.")
@DeprecationDetails.RemovedInVersion("1.1.0")
@Deprecated
package com.falsepattern.lib.updates;

import com.falsepattern.lib.DeprecationDetails;