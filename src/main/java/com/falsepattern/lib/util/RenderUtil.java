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
package com.falsepattern.lib.util;

import com.falsepattern.lib.StableAPI;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static lombok.AccessLevel.PRIVATE;
import static net.minecraft.client.Minecraft.getMinecraft;

@SideOnly(CLIENT)
@NoArgsConstructor(access = PRIVATE)
@StableAPI(since = "0.8.0")
public final class RenderUtil {
    private final static Timer MINECRAFT_TIMER = getMinecraftTimer();

    @SneakyThrows
    private static Timer getMinecraftTimer() {
        val timerField = ReflectionHelper.findField(Minecraft.class, "timer", "field_71428_T");
        timerField.setAccessible(true);
        return (Timer) timerField.get(getMinecraft());
    }

    public static float partialTick() {
        return MINECRAFT_TIMER.renderPartialTicks;
    }
}
