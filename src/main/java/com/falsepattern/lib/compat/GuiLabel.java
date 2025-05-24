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
package com.falsepattern.lib.compat;

import com.falsepattern.lib.StableAPI;
import lombok.NonNull;
import lombok.val;
import lombok.var;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * A functional equivalent to GuiLabel present in Minecraft 1.12.
 */
@SideOnly(Side.CLIENT)
@StableAPI(since = "0.8.0")
public class GuiLabel extends Gui {
    private final List<String> lines = new ArrayList<>();
    private final FontRenderer fontRenderer;
    private final int textColor;
    /**
     * The id of the label.
     */
    @StableAPI.Expose
    public int id;
    /**
     * The x position of the label.
     */
    @StableAPI.Expose
    public int x;
    /**
     * The y position of the label.
     */
    @StableAPI.Expose
    public int y;
    /**
     * The visibility of the label.
     */
    @StableAPI.Expose
    public boolean visible = true;
    /**
     * The label width.
     */
    @StableAPI.Expose
    protected int width;
    /**
     * The label height.
     */
    @StableAPI.Expose
    protected int height;
    private boolean centered = false;

    /**
     * Instantiates a new Gui label.
     *
     * @param fontRenderer the minecraft font renderer
     * @param id           the id
     * @param x            the x
     * @param y            the y
     * @param width        the width
     * @param height       the height
     * @param textColour   the text colour
     */
    @StableAPI.Expose
    public GuiLabel(@NonNull FontRenderer fontRenderer, int id, int x, int y, int width, int height, int textColour) {
        this.fontRenderer = fontRenderer;
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textColor = textColour;
    }

    /**
     * Add a line of text to the GuiLabel.
     *
     * @param text string to add
     */
    @StableAPI.Expose
    public void addLine(@NonNull String text) {
        lines.add(I18n.format(text));
    }

    /**
     * Sets the label text to render centred with respect to the x and y.
     *
     * @return the GuiLabel itself
     */
    @StableAPI.Expose
    public GuiLabel setCentered() {
        centered = true;
        return this;
    }


    /**
     * Draw label.
     *
     * @param minecraft the minecraft
     * @param mouseX    the mouse x
     * @param mouseY    the mouse y
     */
    @StableAPI.Expose
    public void drawLabel(@NonNull Minecraft minecraft, int mouseX, int mouseY) {
        if (!visible) {
            return;
        }
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        val topLeftY = y + height / 2 - lines.size() * 10 / 2;
        for (var i = 0; i < lines.size(); ++i) {
            if (centered) {
                drawCenteredString(fontRenderer, lines.get(i), x + width / 2, topLeftY + i * 10, textColor);
            } else {
                drawString(fontRenderer, lines.get(i), x, topLeftY + i * 10, textColor);
            }
        }
    }
}