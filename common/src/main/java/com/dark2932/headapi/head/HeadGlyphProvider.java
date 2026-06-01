package com.dark2932.headapi.head;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.UUID;

public class HeadGlyphProvider implements GlyphProvider {
    @Override
    public GlyphInfo getGlyph(int codepoint) {
        UUID uuid = HeadCodepointMap.getUUID(codepoint);
        if (uuid == null) {
            return null;
        }
        NativeImage headImage = HeadTextureManager.getHeadImage(uuid);
        if (headImage == null) {
            return null;
        }
        return new HeadGlyphInfo(headImage);
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return HeadCodepointMap.getSupportedCodepoints();
    }
}
