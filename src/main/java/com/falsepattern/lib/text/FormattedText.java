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
package com.falsepattern.lib.text;

import com.falsepattern.lib.StableAPI;
import lombok.NonNull;
import lombok.val;
import lombok.var;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Universal escape sequence-based text rendering and chat messages.
 */
@StableAPI(since = "0.6.0")
public final class FormattedText {
    private static final Map<Character, EnumChatFormatting> reverseMap = new HashMap<>();
    private static final Map<EnumChatFormatting, Color> colorMap = new HashMap<>();

    private static final char ESCAPE = '\u00a7';

    static {
        for (val value : EnumChatFormatting.values()) {
            reverseMap.put(value.getFormattingCode(), value);
            if (value.isColor()) {
                final int rgb;
                switch (value.getFormattingCode()) {
                    default:
                        rgb = 0x000000;
                        break;
                    case '1':
                        rgb = 0x0000AA;
                        break;
                    case '2':
                        rgb = 0x00AA00;
                        break;
                    case '3':
                        rgb = 0x00AAAA;
                        break;
                    case '4':
                        rgb = 0xAA0000;
                        break;
                    case '5':
                        rgb = 0xAA00AA;
                        break;
                    case '6':
                        rgb = 0xFFAA00;
                        break;
                    case '7':
                        rgb = 0xAAAAAA;
                        break;
                    case '8':
                        rgb = 0x555555;
                        break;
                    case '9':
                        rgb = 0x5555FF;
                        break;
                    case 'a':
                        rgb = 0x55FF55;
                        break;
                    case 'b':
                        rgb = 0x55FFFF;
                        break;
                    case 'c':
                        rgb = 0xFF5555;
                        break;
                    case 'd':
                        rgb = 0xFF55FF;
                        break;
                    case 'e':
                        rgb = 0xFFFF55;
                        break;
                    case 'f':
                        rgb = 0xFFFFFF;
                        break;
                }
                colorMap.put(value, new Color(rgb));
            }
            try {
                Field colorF = Color.class.getDeclaredField(value.getFriendlyName());
                colorMap.put(value, (Color) colorF.get(null));
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
    }

    private final String text;

    private final EnumChatFormatting colorStyle;
    private final Set<EnumChatFormatting> fancyStyles = new HashSet<>();

    private final List<FormattedText> siblings = new ArrayList<>();

    private final boolean endLine;

    private FormattedText(@NonNull String text, @NonNull EnumChatFormatting colorStyle, @NonNull Set<EnumChatFormatting> fancyStyles, boolean endLine) {
        this.text = text;
        this.fancyStyles.addAll(fancyStyles);
        this.colorStyle = colorStyle;
        this.endLine = endLine;
    }

    /**
     * Parse a string with minecraft style escapes (\u00a7X) into a {@link FormattedText} instance, which can then be
     * used to generate chat or render-able text.
     *
     * @param text The string to parse
     *
     * @return The parsed text structure
     */
    @StableAPI.Expose
    public static FormattedText parse(String text) {
        EnumChatFormatting currentColorStyle = EnumChatFormatting.WHITE;
        val currentFancyStyle = new HashSet<EnumChatFormatting>();
        FormattedText result = null;
        val accumulator = new StringBuilder();
        int length = text.length();
        boolean format = false;
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (format) {
                val f = reverseMap.get(c);
                if (f == null) {
                    continue;
                }
                if (f == EnumChatFormatting.RESET) {
                    currentColorStyle = EnumChatFormatting.WHITE;
                    currentFancyStyle.clear();
                } else if (f.isColor()) {
                    currentColorStyle = f;
                } else if (f.isFancyStyling()) {
                    currentFancyStyle.add(f);
                }
                format = false;
                continue;
            }
            if (c == ESCAPE || c == '\n') {
                format = c == ESCAPE;
                val txt = new FormattedText(accumulator.toString(), currentColorStyle, currentFancyStyle, c == '\n');
                if (result == null) {
                    result = txt;
                } else {
                    result.siblings.add(txt);
                }
                accumulator.setLength(0);
                continue;
            }
            accumulator.append(c);
        }
        if (accumulator.length() > 0) {
            val txt = new FormattedText(accumulator.toString(), currentColorStyle, currentFancyStyle, true);
            if (result == null) {
                result = txt;
            } else {
                result.siblings.add(txt);
            }
        }
        return result;
    }

    @StableAPI.Expose
    public void addChatMessage(ICommandSender target) {
        addChatMessage(target::addChatMessage);
    }

    private void addChatMessage(Consumer<IChatComponent> consumer) {
        for (val line : toChatText()) {
            consumer.accept(line);
        }
    }

    /**
     * Converts this text structure into chat components that can be sent to the client. This is a list, because chat
     * components can't have newlines.
     *
     * @return The chat component.
     */
    @StableAPI.Expose
    public List<ChatComponentText> toChatText() {
        var thisComponent = toChatTextSingle();
        val result = new ArrayList<ChatComponentText>();
        result.add(thisComponent);
        FormattedText prevSibling = this;
        for (val sibling : siblings) {
            val siblingResult = sibling.toChatTextSingle();
            if (prevSibling.endLine) {
                result.add(thisComponent = siblingResult);
            } else {
                thisComponent.appendSibling(siblingResult);
            }
            prevSibling = sibling;
        }
        return result;
    }

    private ChatComponentText toChatTextSingle() {
        val thisComponent = new ChatComponentText(text);
        val style = new ChatStyle();
        if (colorStyle != null) {
            style.setColor(colorStyle);
        }
        for (val fancyStyle : fancyStyles) {
            switch (fancyStyle) {
                case OBFUSCATED:
                    style.setObfuscated(true);
                    break;
                case BOLD:
                    style.setBold(true);
                    break;
                case STRIKETHROUGH:
                    style.setStrikethrough(true);
                    break;
                case UNDERLINE:
                    style.setUnderlined(true);
                    break;
                case ITALIC:
                    style.setItalic(true);
                    break;
            }
        }
        thisComponent.setChatStyle(style);
        return thisComponent;
    }

    @SideOnly(Side.CLIENT)
    @StableAPI.Expose
    public void addChatMessage(EntityOtherPlayerMP target) {
        addChatMessage(target::addChatMessage);
    }

    @SideOnly(Side.CLIENT)
    @StableAPI.Expose
    public void addChatMessage(EntityPlayerSP target) {
        addChatMessage(target::addChatMessage);
    }

    @StableAPI.Expose
    public void addChatMessage(CommandBlockLogic target) {
        addChatMessage(target::addChatMessage);
    }

    @StableAPI.Expose
    public void addChatMessage(EntityPlayerMP target) {
        addChatMessage(target::addChatMessage);
    }

    @StableAPI.Expose
    public void addChatMessage(RConConsoleSource target) {
        addChatMessage(target::addChatMessage);
    }

    @StableAPI.Expose
    public void addChatMessage(MinecraftServer target) {
        addChatMessage(target::addChatMessage);
    }

    /**
     * {@link #draw(FontRenderer, int, int, boolean)} without drop shadows.
     *
     * @param renderer The font renderer to use
     * @param x        Left side
     * @param y        Top side
     */
    @SideOnly(Side.CLIENT)
    @StableAPI.Expose
    public void draw(FontRenderer renderer, int x, int y) {
        draw(renderer, x, y, false);
    }

    /**
     * Renders this structure as GUI text.
     *
     * @param renderer The font renderer to use
     * @param x        Left side
     * @param y        Top side
     * @param shadow   Whether to have drop shadow under the text
     */
    @SideOnly(Side.CLIENT)
    @StableAPI.Expose
    public void draw(FontRenderer renderer, int x, int y, boolean shadow) {
        int startX = x;
        x = renderer.drawString(text, x, y, colorMap.get(colorStyle).getRGB(), shadow);
        if (endLine) {
            y += renderer.FONT_HEIGHT;
            x = startX;
        }
        for (val sibling : siblings) {
            x = renderer.drawString(sibling.text, x, y, colorMap.get(sibling.colorStyle).getRGB(), shadow);
            if (sibling.endLine) {
                y += renderer.FONT_HEIGHT;
                x = startX;
            }
        }
    }

    /**
     * {@link #draw(FontRenderer, int, int, boolean)} with drop shadows.
     *
     * @param renderer The font renderer to use
     * @param x        Left side
     * @param y        Top side
     */
    @SideOnly(Side.CLIENT)
    @StableAPI.Expose
    public void drawWithShadow(FontRenderer renderer, int x, int y) {
        draw(renderer, x, y, true);
    }
}
