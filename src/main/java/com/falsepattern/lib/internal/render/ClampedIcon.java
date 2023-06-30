package com.falsepattern.lib.internal.render;

import com.falsepattern.lib.util.MathUtil;
import lombok.AllArgsConstructor;
import net.minecraft.util.IIcon;

@AllArgsConstructor
public final class ClampedIcon implements IIcon {
    private final IIcon delegate;

    @Override
    public int getIconWidth() {
        return delegate.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return delegate.getIconHeight();
    }

    @Override
    public float getMinU() {
        return delegate.getMinU();
    }

    @Override
    public float getMaxU() {
        return delegate.getMaxU();
    }

    @Override
    public float getMinV() {
        return delegate.getMinV();
    }

    @Override
    public float getMaxV() {
        return delegate.getMaxV();
    }

    @Override
    public float getInterpolatedU(double textureU) {
        return delegate.getInterpolatedU(clampTextureCoordinate(textureU));
    }

    @Override
    public float getInterpolatedV(double textureV) {
        return delegate.getInterpolatedV(clampTextureCoordinate(textureV));
    }

    @Override
    public String getIconName() {
        return delegate.getIconName();
    }

    private double clampTextureCoordinate(double textureCoordinate) {
        return MathUtil.clamp(textureCoordinate, 0D, 16D);
    }
}
