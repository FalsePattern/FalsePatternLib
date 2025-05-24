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
package com.falsepattern.lib.internal.impl.toast;

import com.falsepattern.lib.internal.config.ToastConfig;
import com.falsepattern.lib.toasts.IToast;
import com.falsepattern.lib.util.MathUtil;
import com.google.common.collect.Queues;
import lombok.val;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Deque;

@SideOnly(Side.CLIENT)
public class GuiToastImpl extends Gui {
    private static GuiToastImpl instance;
    private final Minecraft mc;
    private final Deque<IToast> toastsQueue = Queues.newArrayDeque();
    private ToastInstance<?>[] visible = new ToastInstance[0];

    public GuiToastImpl(Minecraft mcIn) {
        this.mc = mcIn;
    }

    public static void initialize(Minecraft mc) {
        if (instance != null) {
            return;
        }
        instance = new GuiToastImpl(mc);
        FMLCommonHandler.instance().bus().register(instance);
    }

    public static GuiToastImpl getInstance() {
        return instance;
    }

    @SubscribeEvent
    public void updateToasts(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        mc.mcProfiler.startSection("toasts");
        drawToast(new ScaledResolution(mc, mc.displayWidth, mc.displayHeight));
        mc.mcProfiler.endSection();
    }

    public void drawToast(ScaledResolution resolution) {
        if (!this.mc.gameSettings.hideGUI) {
            RenderHelper.disableStandardItemLighting();
            if (ToastConfig.MAX_VISIBLE != visible.length) {
                visible = Arrays.copyOf(visible, ToastConfig.MAX_VISIBLE);
            }

            for (int i = 0; i < this.visible.length; ++i) {
                val toast = this.visible[i];

                if (toast != null && toast.render(ToastConfig.leftAlign() ? 0 : resolution.getScaledWidth(), i)) {
                    this.visible[i] = null;
                }

                if (this.visible[i] == null && !this.toastsQueue.isEmpty()) {
                    this.visible[i] = new ToastInstance<>(this.toastsQueue.removeFirst());
                }
            }
        }
    }

    @Nullable
    public <T extends IToast> T getToast(Class<? extends T> toastClass, Object type) {
        for (val toast : this.visible) {
            if (toast != null && toastClass.isAssignableFrom(toast.getToast().getClass()) && toast.getToast()
                                                                                                  .getType()
                                                                                                  .equals(type)) {
                return toastClass.cast(toast);
            }
        }

        for (val toast : this.toastsQueue) {
            if (toastClass.isAssignableFrom(toast.getClass()) && toast.getType().equals(type)) {
                return toastClass.cast(toast);
            }
        }

        return null;
    }

    public void clear() {
        Arrays.fill(this.visible, null);
        this.toastsQueue.clear();
    }

    public void add(IToast toastIn) {
        this.toastsQueue.add(toastIn);
    }

    public Minecraft getMinecraft() {
        return this.mc;
    }

    @SideOnly(Side.CLIENT)
    class ToastInstance<T extends IToast> {
        private final T toast;
        private long animationTime;
        private long visibleTime;
        private IToast.Visibility visibility;

        private ToastInstance(T toastIn) {
            this.animationTime = -1L;
            this.visibleTime = -1L;
            this.visibility = IToast.Visibility.SHOW;
            this.toast = toastIn;
        }

        public T getToast() {
            return this.toast;
        }

        private float getVisibility(long currentTime) {
            var visibility = MathUtil.clamp((float) (currentTime - this.animationTime) / 600.0F, 0.0F, 1.0F);
            visibility = visibility * visibility;
            return this.visibility == IToast.Visibility.HIDE ? 1.0F - visibility : visibility;
        }

        public boolean render(int x, int y) {
            val sysTime = Minecraft.getSystemTime();

            if (this.animationTime == -1L) {
                this.animationTime = sysTime;
                this.visibility.playSound(GuiToastImpl.this.mc.getSoundHandler());
            }

            if (this.visibility == IToast.Visibility.SHOW && sysTime - this.animationTime <= 600L) {
                this.visibleTime = sysTime;
            }

            GL11.glPushMatrix();
            val shift = toast.width() * getVisibility(sysTime);
            val X = ToastConfig.leftAlign() ? shift - toast.width() : x - shift;
            GL11.glTranslatef(X, (float) (y * 32) + ToastConfig.Y_OFFSET, (float) (500 + y));
            val visibility = this.toast.draw(GuiToastImpl.this, sysTime - this.visibleTime);
            GL11.glPopMatrix();

            if (visibility != this.visibility) {
                this.animationTime = sysTime - (long) ((int) ((1.0F - this.getVisibility(sysTime)) * 600.0F));
                this.visibility = visibility;
                this.visibility.playSound(GuiToastImpl.this.mc.getSoundHandler());
            }

            return this.visibility == IToast.Visibility.HIDE && sysTime - this.animationTime > 600L;
        }
    }
}