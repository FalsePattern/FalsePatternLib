package com.falsepattern.lib.util;

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
