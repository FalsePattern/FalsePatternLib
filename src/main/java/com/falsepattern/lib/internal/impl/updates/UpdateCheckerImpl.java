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

package com.falsepattern.lib.internal.impl.updates;

import com.falsepattern.lib.internal.Internet;
import com.falsepattern.lib.internal.config.LibraryConfig;
import com.falsepattern.lib.text.FormattedText;
import com.falsepattern.lib.updates.ModUpdateInfo;
import com.falsepattern.lib.updates.UpdateCheckException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.val;
import lombok.var;

import net.minecraft.client.resources.I18n;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.IChatComponent;
import cpw.mods.fml.common.Loader;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

public final class UpdateCheckerImpl {
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

    private static JsonArray fetchRootListShared(String url) {
        if (!LibraryConfig.ENABLE_UPDATE_CHECKER) {
            throw new CompletionException(new UpdateCheckException("Update checker is disabled in config!"));
        }
        URL URL;
        try {
            URL = new URL(url);
        } catch (MalformedURLException e) {
            throw new CompletionException(new UpdateCheckException("Invalid URL: " + url, e));
        }
        JsonElement parsed;
        try {
            parsed = new JsonParser().parse(Internet.download(URL).thenApply(String::new).join());
        } catch (CompletionException e) {
            throw new CompletionException(new UpdateCheckException("Failed to download update checker JSON file!",
                                                                   e.getCause() == null ? e : e.getCause()));
        }
        JsonArray modList;
        if (parsed.isJsonArray()) {
            modList = parsed.getAsJsonArray();
        } else {
            modList = new JsonArray();
            modList.add(parsed);
        }
        return modList;
    }

    public static CompletableFuture<List<ModUpdateInfo>> fetchUpdatesAsync(String url) {
        return CompletableFuture.supplyAsync(() -> {
            val modList = fetchRootListShared(url);
            val result = new ArrayList<ModUpdateInfo>();
            val installedMods = Loader.instance().getIndexedModList();
            for (val node : modList) {
                if (!node.isJsonObject()) {
                    continue;
                }
                val obj = node.getAsJsonObject();
                if (!obj.has("modid")) {
                    continue;
                }
                if (!obj.has("latestVersion")) {
                    continue;
                }
                val modid = obj.get("modid").getAsString();
                if (!installedMods.containsKey(modid)) {
                    continue;
                }
                val mod = installedMods.get(modid);
                val latestVersionsNode = obj.get("latestVersion");
                List<String> latestVersions;
                if (latestVersionsNode.isJsonPrimitive() && (latestVersionsNode.getAsJsonPrimitive().isString())) {
                    latestVersions = Collections.singletonList(latestVersionsNode.getAsString());
                } else if (latestVersionsNode.isJsonArray()) {
                    latestVersions = new ArrayList<>();
                    for (val version : latestVersionsNode.getAsJsonArray()) {
                        if (!version.isJsonPrimitive() || !version.getAsJsonPrimitive().isString()) {
                            continue;
                        }
                        latestVersions.add(version.getAsString());
                    }
                } else {
                    continue;
                }
                val currentVersion = mod.getVersion();
                if (latestVersions.contains(currentVersion)) {
                    continue;
                }
                val updateURL = obj.has("updateURL") &&
                                obj.get("updateURL").isJsonPrimitive() &&
                                obj.get("updateURL").getAsJsonPrimitive().isString()
                                ? obj.get("updateURL").getAsString()
                                : "";
                result.add(new ModUpdateInfo(modid, currentVersion, latestVersions.get(0), updateURL));
            }
            return result;
        });
    }

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

    public static CompletableFuture<List<ModUpdateInfo>> fetchUpdatesAsyncV2(String url) {
        return CompletableFuture.supplyAsync(() -> {
            val modList = fetchRootListShared(url);
            val result = new ArrayList<ModUpdateInfo>();
            val installedMods = Loader.instance().getIndexedModList();
            for (val node : modList) {
                if (!node.isJsonObject()) {
                    continue;
                }
                val obj = node.getAsJsonObject();
                if (!obj.has("modid")) {
                    continue;
                }
                if (!obj.has("versions")) {
                    continue;
                }
                val modid = obj.get("modid").getAsString();
                if (!installedMods.containsKey(modid)) {
                    continue;
                }
                val mod = installedMods.get(modid);
                var versions = obj.get("versions");
                if (!versions.isJsonArray()) {
                    if (!versions.isJsonObject()) {
                        continue;
                    }
                    val ver = versions.getAsJsonObject();
                    versions = new JsonArray();
                    versions.getAsJsonArray().add(ver);
                }
                val parsedVersions = parseVersionSpecs(versions.getAsJsonArray()).join();
                val latestVersions = new ArrayList<String>();
                for (val version : parsedVersions) {
                    val versionObj = version.getAsJsonObject();
                    latestVersions.add(versionObj.get("version").getAsString());
                }
                val currentVersion = mod.getVersion();
                if (latestVersions.contains(currentVersion)) {
                    continue;
                }
                val latest = parsedVersions.get(0).getAsJsonObject();
                result.add(new ModUpdateInfo(modid, currentVersion, latest.get("version").getAsString(), latest.get("url").getAsString()));
            }
            return result;
        });
    }

    private static String getString(JsonObject object, String entry) {
        if (!object.has(entry)) {
            return null;
        }
        val element = object.get(entry);
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return element.getAsString();
        }
        return null;
    }

    private static CompletableFuture<JsonArray> parseVersionSpecs(JsonArray versions) {
        return CompletableFuture.supplyAsync(() -> {
            val result = new JsonArray();
            for (val entry: versions) {
                if (!entry.isJsonObject()) {
                    continue;
                }
                val obj = entry.getAsJsonObject();
                val type = getString(obj, "type");
                if (type == null) {
                    continue;
                }
                switch (type) {
                    case "raw": {
                        val version = getString(obj, "version");
                        val url = getString(obj, "updateURL");
                        if (version == null || url == null) {
                            continue;
                        }
                        val res = new JsonObject();
                        res.addProperty("version", version);
                        res.addProperty("url", url);
                        result.add(res);
                    }
                    case "github": {
                        val repo = getString(obj, "repo");
                        if (repo == null) {
                            continue;
                        }
                        JsonObject response;
                        try {
                            response = parseLatestGithubVersion(repo);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            continue;
                        }
                        if (response == null) {
                            continue;
                        }
                        result.add(response);
                    }
                }
            }
            return result;
        });
    }

    private static JsonObject parseLatestGithubVersion(String repo) throws MalformedURLException {
        val parts = repo.split("/");
        if (parts.length != 2) {
            return null;
        }
        val owner = parts[0];
        val name = parts[1];
        val response = new AtomicReference<JsonObject>();
        val result = new JsonObject();
        Internet.connect(new URL("https://api.github.com/repos/" + owner + "/" + name + "/releases/latest"),
                         Internet.constructHeaders("Accept", "application/vnd.github+json",
                                                   "X-GitHub-Api-Version", "2022-11-28"),
                         (e) -> {throw new CompletionException(e);
                         },
                         (stream) -> {
            val parser = new JsonParser();
            val json = parser.parse(new InputStreamReader(stream));
            if (!json.isJsonObject()) {
                return;
            }
            response.set(json.getAsJsonObject());
                         });
        if (response.get() == null) {
            return null;
        }
        val payload = response.get();
        val url = getString(payload, "html_url");
        val version = getString(payload, "tag_name");
        if (version == null) {
            return null;
        }
        result.addProperty("version", version);
        result.addProperty("url", url == null ? "" : url);
        return result;
    }

    public static List<ModUpdateInfo> fetchUpdatesV2(String url) throws UpdateCheckException {
        try {
            return fetchUpdatesAsyncV2(url).join();
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
}
