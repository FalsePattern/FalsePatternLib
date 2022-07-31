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

import com.falsepattern.lib.internal.impl.config.ConfigurationManagerImpl;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

public class SyncRequest extends CompressedMessage {
    public List<Class<?>> matchingClassesOnOurSide;

    @Override
    protected void transmit(DataOutput output) throws IOException {
        ConfigurationManagerImpl.sendRequest(output);
    }

    @Override
    protected void receive(DataInput input) throws IOException {
        matchingClassesOnOurSide = ConfigurationManagerImpl.receiveRequest(input);
    }
}
