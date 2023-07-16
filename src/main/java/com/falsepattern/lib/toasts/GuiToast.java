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
package com.falsepattern.lib.toasts;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.impl.toast.GuiToastImpl;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
@StableAPI(since = "0.10.0")
public class GuiToast {
    @Nullable
    @StableAPI.Expose
    public static <T extends IToast> T getToast(Class<? extends T> toastClass, Object type) {
        return GuiToastImpl.getInstance().getToast(toastClass, type);
    }

    @StableAPI.Expose
    public static void clear() {
        GuiToastImpl.getInstance().clear();
    }

    @StableAPI.Expose
    public static void add(IToast toastIn) {
        GuiToastImpl.getInstance().add(toastIn);
    }
}
