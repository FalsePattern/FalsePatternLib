/*
 * This file is part of FalsePatternLib.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalsePatternLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * FalsePatternLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalsePatternLib. If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.lib.util;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.Tags;
import com.falsepattern.lib.internal.render.ClampedIcon;
import com.falsepattern.lib.internal.render.FullTextureIcon;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.util.Timer;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.SideOnly;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static net.minecraft.client.Minecraft.getMinecraft;

@SideOnly(CLIENT)
@UtilityClass
@StableAPI(since = "0.8.0")
public final class RenderUtil {
    private static final Timer MINECRAFT_TIMER = getMinecraftTimer();
    private static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation(Tags.MODID, "textures/empty_texture.png");

    /**
     * Sets the OpenGL translation, relative to the player's position.
     * <p>
     * This is useful for rendering things that are not part of the world mesh, but should be rendered as if they were.
     * <p>
     * It's good practice to make this call inside a {@link GL11#glPushMatrix() push}/{@link GL11#glPopMatrix() pop} matrix block.
     */
    @StableAPI.Expose(since = "0.12.0")
    public static void setGLTranslationRelativeToPlayer() {
        val player = getMinecraft().thePlayer;
        val partialTick = partialTick();

        val offsetX = (float) (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTick);
        val offsetY = (float) (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTick);
        val offsetZ = (float) (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTick);

        GL11.glTranslatef(-offsetX, -offsetY, -offsetZ);
    }

    /**
     * Provides a texture icon with the given name and dimensions.
     * <p>
     * This is useful for rendering textures that are not part of the Minecraft texture atlas.
     *
     * @param iconName The icon name
     * @param width    The icon width in pixels
     * @param height   The icon height in pixels
     *
     * @return The full resolution texture icon.
     */
    @StableAPI.Expose(since = "0.10.0")
    public static IIcon getFullTextureIcon(String iconName, int width, int height) {
        return new FullTextureIcon(iconName, width, height);
    }

    /**
     * Wraps the given icon as a clamped icon.
     * <p>
     * A clamped icon will clamp the coordinates given to {@link IIcon#getInterpolatedU(double)} and {@link IIcon#getInterpolatedV(double)} to the range of 0 to 16.
     * <p>
     * This is helpful when using {@link RenderBlocks} but having different bounds.
     *
     * @param icon The icon to clamp
     */
    @StableAPI.Expose(since = "0.12.0")
    public static IIcon wrapAsClampedIcon(IIcon icon) {
        return new ClampedIcon(icon);
    }

    /**
     * Binds an empty texture.
     * <p>
     * When rendering without shaders, using {@link GL11#glDisable(int)} with {@link GL11#GL_TEXTURE_2D}
     * is sufficient to achieve the same effect.
     * <p>
     * However, when shaders are enabled, disabling textures using this method will have no effect. Therefore this method can be used as a workaround.
     */
    @StableAPI.Expose(since = "1.3.0")
    public static void bindEmptyTexture() {
        getMinecraft().renderEngine.bindTexture(EMPTY_TEXTURE);
    }

    /**
     * Provides the partial tick between the last and next client tick, in the range of 0 to 1.
     * <p>
     * Sometimes referred to as 'subTick', it is used mostly for interpolation in rendering.
     *
     * @return The current partial tick
     */
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
