/**
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 * <p>
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.lib.internal.impl.config.net;

import com.falsepattern.lib.internal.FalsePatternLib;
import lombok.val;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

import java.io.IOException;

public class SyncRequestHandler implements IMessageHandler<SyncRequest, IMessage> {
    @Override
    public IMessage onMessage(SyncRequest message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            //Do not sync client to server
            return null;
        }
        try {
            message.receive();
            val reply = new SyncReply();
            reply.matchingClassesOnOurSide = message.matchingClassesOnOurSide;
            reply.transmit();
            FalsePatternLib.NETWORK.sendTo(reply, ctx.getServerHandler().playerEntity);
            return null;
        } catch (IOException e) {
            FalsePatternLib.getLog().error("Failed to sync config", e);
            return null;
        }
    }
}
