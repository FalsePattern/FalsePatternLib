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
package com.falsepattern.lib.util;

import com.falsepattern.lib.StableAPI;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IIcon;
import net.minecraft.util.Timer;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static net.minecraft.client.Minecraft.getMinecraft;

@SideOnly(CLIENT)
@UtilityClass
@StableAPI(since = "0.8.0")
public final class RenderUtil {
    private static final Timer MINECRAFT_TIMER = getMinecraftTimer();

    @StableAPI.Expose(since = "0.12.0")
    public static void setGLTranslationRelativeToPlayer() {
        val player = getMinecraft().thePlayer;
        val partialTick = partialTick();

        val offsetX = (float) (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTick);
        val offsetY = (float) (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTick);
        val offsetZ = (float) (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTick);

        GL11.glTranslatef(-offsetX, -offsetY, -offsetZ);
    }

    @StableAPI.Expose(since = "0.10.0")
    public static IIcon getFullTextureIcon(String iconName, int width, int height) {
        return new IIcon() {
            @Override
            public int getIconWidth() {
                return width;
            }

            @Override
            public int getIconHeight() {
                return height;
            }

            @Override
            public float getMinU() {
                return 0;
            }

            @Override
            public float getMaxU() {
                return 1;
            }

            @Override
            public float getInterpolatedU(double u) {
                return (float) (u / 16D);
            }

            @Override
            public float getMinV() {
                return 0;
            }

            @Override
            public float getMaxV() {
                return 1;
            }

            @Override
            public float getInterpolatedV(double v) {
                return (float) (v / 16D);
            }

            @Override
            public String getIconName() {
                return iconName;
            }
        };
    }

    @StableAPI.Expose
    public static float partialTick() {
        return MINECRAFT_TIMER.renderPartialTicks;
    }

    @StableAPI.Expose
    @SneakyThrows
    private static Timer getMinecraftTimer() {
        val timerField = ReflectionHelper.findField(Minecraft.class, "timer", "field_71428_T");
        timerField.setAccessible(true);
        return (Timer) timerField.get(getMinecraft());
    }
}
