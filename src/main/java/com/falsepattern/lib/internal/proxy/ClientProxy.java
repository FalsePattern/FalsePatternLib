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
package com.falsepattern.lib.internal.proxy;

import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.updates.UpdateChecker;
import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.val;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    private CompletableFuture<List<IChatComponent>> chatFuture;

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        chatFuture = updatesFuture.handleAsync((updates, exception) -> {
            if (exception != null || updates.isEmpty()) {
                return Collections.emptyList();
            }
            return UpdateChecker.updateListToChatMessages(Tags.MODNAME, updates);
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
