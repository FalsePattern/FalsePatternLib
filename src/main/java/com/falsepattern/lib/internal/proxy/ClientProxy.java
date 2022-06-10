package com.falsepattern.lib.internal.proxy;

import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.text.FormattedText;
import com.falsepattern.lib.util.Async;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.val;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    private Future<List<IChatComponent>> chatFuture;

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        chatFuture = Async.asyncWorker.submit(() -> {
            long start = System.nanoTime();
            val updates = updatesFuture.get();
            if (updates == null || updates.size() == 0) return null;
            val updateText = new ArrayList<IChatComponent>(FormattedText.parse(I18n.format("falsepatternlib.chat.updatesavailable")).toChatText());
            val mods = Loader.instance().getIndexedModList();
            for (val update : updates) {
                val mod = mods.get(update.modID);
                updateText.addAll(FormattedText.parse(I18n.format("falsepatternlib.chat.modname", mod.getName())).toChatText());
                updateText.addAll(FormattedText.parse(I18n.format("falsepatternlib.chat.currentversion", update.currentVersion)).toChatText());
                updateText.addAll(FormattedText.parse(I18n.format("falsepatternlib.chat.latestversion", update.latestVersion)).toChatText());
                if (!update.updateURL.isEmpty()) {
                    val pre = FormattedText.parse(I18n.format("falsepatternlib.chat.updateurlpre")).toChatText();
                    val link = FormattedText.parse(I18n.format("falsepatternlib.chat.updateurl")).toChatText();
                    val post = FormattedText.parse(I18n.format("falsepatternlib.chat.updateurlpost")).toChatText();
                    pre.get(pre.size() - 1).appendSibling(link.get(0));
                    link.get(link.size() - 1).appendSibling(post.get(0));
                    for (val l: link) {
                        l.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, update.updateURL));
                    }
                    link.remove(0);
                    post.remove(0);
                    updateText.addAll(pre);
                    updateText.addAll(link);
                    updateText.addAll(post);
                }
            }
            long end = System.nanoTime();
            FalsePatternLib.getLog().info("Constructed in {} ms", (end - start) / 1000000L);
            return updateText;
        });
    }

    @SubscribeEvent
    public void onSinglePlayer(EntityJoinWorldEvent e) {
        if (chatFuture == null ||
            !(e.entity instanceof EntityPlayerSP)) return;
        val player = (EntityPlayerSP) e.entity;
        try {
            for (val line: chatFuture.get()) {
                player.addChatMessage(line);
            }
            chatFuture = null;
        } catch (Exception ex) {
            FalsePatternLib.getLog().warn(ex);
        }
    }
}
