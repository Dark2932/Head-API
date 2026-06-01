package com.dark2932.headapi.mixin;

import com.dark2932.headapi.head.HeadTextureManager;
import com.dark2932.headapi.mixininterface.TextureLocationSettable;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HttpTexture.class)
public class HttpTextureMixin implements TextureLocationSettable {
    @Unique
    private ResourceLocation headapi$skinLocation;

    @Override
    public void headapi$setTextureLocation(ResourceLocation location) {
        this.headapi$skinLocation = location;
    }

    @Override
    public ResourceLocation headapi$getTextureLocation() {
        return this.headapi$skinLocation;
    }

    @Inject(method = "loadCallback", at = @At("HEAD"))
    private void headapi$extractHead(NativeImage image, CallbackInfo ci) {
        if (this.headapi$skinLocation != null && image != null) {
            HeadTextureManager.onSkinLoaded(this.headapi$skinLocation, image);
        }
    }
}
