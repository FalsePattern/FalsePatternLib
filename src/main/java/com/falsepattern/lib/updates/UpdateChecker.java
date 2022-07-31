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

import com.falsepattern.json.node.JsonNode;
import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.dependencies.DependencyLoader;
import com.falsepattern.lib.dependencies.SemanticVersion;
import com.falsepattern.lib.internal.Internet;
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.config.LibraryConfig;
import com.falsepattern.lib.text.FormattedText;
import lombok.val;

import net.minecraft.client.resources.I18n;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.IChatComponent;
import cpw.mods.fml.common.Loader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

@StableAPI(since = "0.8.0")
public class UpdateChecker {
    private static final AtomicBoolean jsonLibraryLoaded = new AtomicBoolean(false);


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
    public static CompletableFuture<List<ModUpdateInfo>> fetchUpdatesAsync(String url) {
        return CompletableFuture.supplyAsync(() -> {
            if (!LibraryConfig.ENABLE_UPDATE_CHECKER) {
                throw new CompletionException(new UpdateCheckException("Update checker is disabled in config!"));
            }
            URL URL;
            try {
                URL = new URL(url);
            } catch (MalformedURLException e) {
                throw new CompletionException(new UpdateCheckException("Invalid URL: " + url, e));
            }
            if (!jsonLibraryLoaded.get()) {
                try {
                    DependencyLoader.addMavenRepo("https://maven.falsepattern.com/");
                    DependencyLoader.builder()
                                    .loadingModId(Tags.MODID)
                                    .groupId("com.falsepattern")
                                    .artifactId("json")
                                    .minVersion(new SemanticVersion(0, 4, 0))
                                    .maxVersion(new SemanticVersion(0, Integer.MAX_VALUE, Integer.MAX_VALUE))
                                    .preferredVersion(new SemanticVersion(0, 4, 1))
                                    .build();
                } catch (Exception e) {
                    throw new CompletionException(
                            new UpdateCheckException("Failed to load json library for update checker!", e));
                }
                jsonLibraryLoaded.set(true);
            }
            val result = new ArrayList<ModUpdateInfo>();
            JsonNode parsed;
            try {
                parsed = JsonNode.parse(Internet.download(URL).thenApply(String::new).join());
            } catch (CompletionException e) {
                throw new CompletionException(new UpdateCheckException("Failed to download update checker JSON file!",
                                                                       e.getCause() == null ? e : e.getCause()));
            }
            List<JsonNode> modList;
            if (parsed.isList()) {
                modList = parsed.getJavaList();
            } else {
                modList = Collections.singletonList(parsed);
            }
            val installedMods = Loader.instance().getIndexedModList();
            for (val node : modList) {
                if (!node.isObject()) {
                    continue;
                }
                if (!node.containsKey("modid")) {
                    continue;
                }
                if (!node.containsKey("latestVersion")) {
                    continue;
                }
                val modid = node.getString("modid");
                if (!installedMods.containsKey(modid)) {
                    continue;
                }
                val mod = installedMods.get(modid);
                val latestVersionsNode = node.get("latestVersion");
                List<String> latestVersions;
                if (latestVersionsNode.isString()) {
                    latestVersions = Collections.singletonList(latestVersionsNode.stringValue());
                } else if (latestVersionsNode.isList()) {
                    latestVersions = new ArrayList<>();
                    for (val version : latestVersionsNode.getJavaList()) {
                        if (!version.isString()) {
                            continue;
                        }
                        latestVersions.add(version.stringValue());
                    }
                } else {
                    continue;
                }
                val currentVersion = mod.getVersion();
                if (latestVersions.contains(currentVersion)) {
                    continue;
                }
                val updateURL =
                        node.containsKey("updateURL") && node.get("updateURL").isString() ? node.getString("updateURL")
                                                                                          : "";
                result.add(new ModUpdateInfo(modid, currentVersion, latestVersions.get(0), updateURL));
            }
            return result;
        });
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
    public static List<ModUpdateInfo> fetchUpdates(String url) throws UpdateCheckException {
        try {
            return fetchUpdatesAsync(url).join();
        } catch (CompletionException e) {
            try {
                throw e.getCause();
            } catch (UpdateCheckException e1) {
                throw e1;
            } catch (Throwable e1) {
                throw new UpdateCheckException("Failed to check for updates!", e1);
            }
        }
    }

    /**
     * Formats the raw list of updates into lines of chat messages you can send to players.
     *
     * @param initiator Who/what/which mod did this update check
     * @param updates   The list of updates to convert
     *
     * @return A list of chat messages that can be sent to players
     */
    public static List<IChatComponent> updateListToChatMessages(String initiator, List<ModUpdateInfo> updates) {
        if (updates == null || updates.size() == 0) {
            return null;
        }
        val updateText = new ArrayList<IChatComponent>(
                FormattedText.parse(I18n.format("falsepatternlib.chat.updatesavailable", initiator)).toChatText());
        val mods = Loader.instance().getIndexedModList();
        for (val update : updates) {
            val mod = mods.get(update.modID);
            updateText.addAll(
                    FormattedText.parse(I18n.format("falsepatternlib.chat.modname", mod.getName())).toChatText());
            updateText.addAll(
                    FormattedText.parse(I18n.format("falsepatternlib.chat.currentversion", update.currentVersion))
                                 .toChatText());
            updateText.addAll(
                    FormattedText.parse(I18n.format("falsepatternlib.chat.latestversion", update.latestVersion))
                                 .toChatText());
            if (!update.updateURL.isEmpty()) {
                val pre = FormattedText.parse(I18n.format("falsepatternlib.chat.updateurlpre")).toChatText();
                val link = FormattedText.parse(I18n.format("falsepatternlib.chat.updateurl")).toChatText();
                val post = FormattedText.parse(I18n.format("falsepatternlib.chat.updateurlpost")).toChatText();
                pre.get(pre.size() - 1).appendSibling(link.get(0));
                link.get(link.size() - 1).appendSibling(post.get(0));
                for (val l : link) {
                    l.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, update.updateURL));
                }
                link.remove(0);
                post.remove(0);
                updateText.addAll(pre);
                updateText.addAll(link);
                updateText.addAll(post);
            }
        }
        return updateText;
    }
}
