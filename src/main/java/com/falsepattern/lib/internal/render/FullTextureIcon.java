package com.falsepattern.lib.internal.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.IIcon;

@Getter
@AllArgsConstructor
public final class FullTextureIcon implements IIcon {
    private final String iconName;

    private final int iconWidth;
    private final int iconHeight;

    @Override
    public float getMinU() {
        return 0F;
    }

    @Override
    public float getMinV() {
        return 0F;
    }

    @Override
    public float getMaxU() {
        return 1F;
    }

    @Override
    public float getMaxV() {
        return 1F;
    }

    @Override
    public float getInterpolatedU(double textureU) {
        return (float) textureU / 16F;
    }

    @Override
    public float getInterpolatedV(double textureV) {
        return (float) textureV / 16F;
    }
}
