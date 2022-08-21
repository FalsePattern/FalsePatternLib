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
package com.falsepattern.lib.internal.impl.config.net;

import io.netty.buffer.ByteBuf;
import lombok.val;

import cpw.mods.fml.common.network.simpleimpl.IMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public abstract class CompressedMessage implements IMessage {
    private byte[] compressed;

    public CompressedMessage() {
        compressed = null;
    }

    protected abstract void transmit(DataOutput output) throws IOException;

    protected abstract void receive(DataInput input) throws IOException;

    public void transmit() throws IOException {
        val output = new ByteArrayOutputStream();
        val deflateOut = new DeflaterOutputStream(output, new Deflater(Deflater.BEST_COMPRESSION, false));
        val dataOut = new DataOutputStream(deflateOut);
        transmit(dataOut);
        dataOut.close();
        compressed = output.toByteArray();
    }

    public void receive() throws IOException {
        val input = new ByteArrayInputStream(compressed);
        val deflateIn = new InflaterInputStream(input, new Inflater(false));
        val dataIn = new DataInputStream(deflateIn);
        receive(dataIn);
        dataIn.close();
        compressed = null;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int length = buf.readInt();
        compressed = new byte[length];
        buf.readBytes(compressed);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(compressed.length);
        buf.writeBytes(compressed);
    }
}
