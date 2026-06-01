package com.dark2932.headapi.mixin;

import com.dark2932.headapi.head.HeadGlyphProvider;
import com.mojang.blaze3d.font.GlyphProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FontSet.class)
public class FontSetMixin {
    @Shadow
    @Final
    private ResourceLocation name;

    @Shadow
    private List<GlyphProvider> activeProviders;

    @Inject(method = "reload(Ljava/util/Set;)V", at = @At("RETURN"))
    private void headapi$addHeadProvider(Set<FontOption> filter, CallbackInfo ci) {
        if (Style.DEFAULT_FONT.equals(this.name)) {
            List<GlyphProvider> list = new ArrayList<>(this.activeProviders);
            list.add(new HeadGlyphProvider());
            this.activeProviders = list;
        }
    }
}
