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
package com.falsepattern.lib.internal.impl.config.net;

import com.falsepattern.lib.internal.FalsePatternLib;
import com.falsepattern.lib.internal.Share;
import lombok.val;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

import java.io.IOException;
import java.util.Objects;

public class SyncRequestHandler implements IMessageHandler<SyncRequest, IMessage> {
    @Override
    public IMessage onMessage(SyncRequest message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            //Do not sync client to server
            return null;
        }
        if (Objects.requireNonNull(FMLCommonHandler.instance().getSide()) == Side.CLIENT) {
            //Integrated server only syncs to open-to-lan joiners
            if (isLocalPlayer(ctx.getServerHandler().playerEntity)) {
                return null;
            }
        }
        try {
            message.receive();
            val reply = new SyncReply();
            reply.matchingClassesOnOurSide = message.matchingClassesOnOurSide;
            reply.transmit();
            FalsePatternLib.NETWORK.sendTo(reply, ctx.getServerHandler().playerEntity);
            return null;
        } catch (IOException e) {
            Share.LOG.error("Failed to sync config", e);
            return null;
        }
    }

    private static boolean isLocalPlayer(EntityPlayerMP playerMP) {
        val localPlayer = Minecraft.getMinecraft().thePlayer;
        if (localPlayer == null)
            return false;
        val remoteUUID = playerMP.getUniqueID();
        val localUUID = localPlayer.getUniqueID();
        return Objects.equals(remoteUUID, localUUID);
    }
}
