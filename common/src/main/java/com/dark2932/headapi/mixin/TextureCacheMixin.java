package com.dark2932.headapi.mixin;

import com.dark2932.headapi.mixininterface.TextureLocationSettable;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.resources.SkinManager$TextureCache")
public class TextureCacheMixin {
    @Shadow
    @Final
    private MinecraftProfileTexture.Type type;

    @Redirect(
        method = "registerTexture",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureManager;register(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/texture/AbstractTexture;)V"
        )
    )
    private void headapi$registerAndTag(TextureManager manager, ResourceLocation location, AbstractTexture texture) {
        if (this.type == MinecraftProfileTexture.Type.SKIN && texture instanceof HttpTexture httpTexture) {
            ((TextureLocationSettable) httpTexture).headapi$setTextureLocation(location);
        }
        manager.register(location, texture);
    }
}
