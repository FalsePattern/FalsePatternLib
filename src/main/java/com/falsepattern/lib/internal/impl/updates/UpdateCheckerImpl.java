package com.falsepattern.lib.internal.impl.updates;

import com.falsepattern.json.node.JsonNode;
import com.falsepattern.lib.dependencies.DependencyLoader;
import com.falsepattern.lib.dependencies.Library;
import com.falsepattern.lib.dependencies.SemanticVersion;
import com.falsepattern.lib.internal.Internet;
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.config.LibraryConfig;
import com.falsepattern.lib.text.FormattedText;
import com.falsepattern.lib.updates.ModUpdateInfo;
import com.falsepattern.lib.updates.UpdateCheckException;
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

public final class UpdateCheckerImpl {
    private static final AtomicBoolean jsonLibraryLoaded = new AtomicBoolean(false);

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
                    DependencyLoader.loadLibraries(Library.builder()
                                                          .loadingModId(Tags.MODID)
                                                          .groupId("com.falsepattern")
                                                          .artifactId("json")
                                                          .minVersion(new SemanticVersion(0, 4, 0))
                                                          .maxVersion(new SemanticVersion(0, Integer.MAX_VALUE, Integer.MAX_VALUE))
                                                          .preferredVersion(new SemanticVersion(0, 4, 1))
                                                          .build());
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
}
