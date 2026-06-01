package com.dark2932.headapi.mixin;

import com.dark2932.headapi.head.HeadTextureManager;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkinManager.class)
public class SkinManagerMixin {
    @Inject(method = "registerTextures", at = @At("HEAD"))
    private void headapi$trackUUID(UUID uuid, MinecraftProfileTextures textures, CallbackInfoReturnable<CompletableFuture<PlayerSkin>> cir) {
        MinecraftProfileTexture skin = textures.skin();
        if (skin != null) {
            HeadTextureManager.associateUUID(uuid, skin.getHash());
        }
    }
}
