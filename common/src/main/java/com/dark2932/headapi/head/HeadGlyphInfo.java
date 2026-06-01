package com.dark2932.headapi.head;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.function.Function;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;

public class HeadGlyphInfo implements GlyphInfo {
    private static final float HEAD_SIZE = 8.0F;
    private final NativeImage headImage;

    public HeadGlyphInfo(NativeImage headImage) {
        this.headImage = headImage;
    }

    @Override
    public float getAdvance() {
        return HEAD_SIZE;
    }

    @Override
    public float getAdvance(boolean bold) {
        return HEAD_SIZE + (bold ? getBoldOffset() : 0.0F);
    }

    @Override
    public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> stitcher) {
        return stitcher.apply(new SheetGlyphInfo() {
            @Override
            public int getPixelWidth() {
                return headImage.getWidth();
            }

            @Override
            public int getPixelHeight() {
                return headImage.getHeight();
            }

            @Override
            public float getOversample() {
                return 1.0F;
            }

            @Override
            public void upload(int x, int y) {
                headImage.upload(0, x, y, false);
            }

            @Override
            public boolean isColored() {
                return true;
            }

            @Override
            public float getBearingLeft() {
                return 0.0F;
            }

            @Override
            public float getBearingTop() {
                return HEAD_SIZE;
            }
        });
    }
}
