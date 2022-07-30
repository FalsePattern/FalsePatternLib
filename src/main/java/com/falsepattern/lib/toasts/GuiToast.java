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
package com.falsepattern.lib.toasts;

import com.falsepattern.lib.StableAPI;
import com.falsepattern.lib.internal.impl.toast.GuiToastImpl;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
@StableAPI(since = "0.10.0")
public class GuiToast {
    @StableAPI(since = "0.10.0")
    @Nullable
    public static <T extends IToast> T getToast(Class<? extends T> toastClass, Object type) {
        return GuiToastImpl.getInstance().getToast(toastClass, type);
    }

    @StableAPI(since = "0.10.0")
    public static void clear() {
        GuiToastImpl.getInstance().clear();
    }

    @StableAPI(since = "0.10.0")
    public static void add(IToast toastIn) {
        GuiToastImpl.getInstance().add(toastIn);
    }
}
