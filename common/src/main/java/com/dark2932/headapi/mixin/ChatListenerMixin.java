package com.dark2932.headapi.mixin;

import com.dark2932.headapi.ChatHeadHandler;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatListener.class)
public class ChatListenerMixin {
    @Unique
    private GameProfile headapi$sender;

    @Inject(method = "handlePlayerChatMessage", at = @At("HEAD"))
    private void headapi$captureSender(PlayerChatMessage message, GameProfile sender, ChatType.Bound chatType, CallbackInfo ci) {
        this.headapi$sender = sender;
    }

    @ModifyArg(
        method = "handlePlayerChatMessage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/chat/ChatType$Bound;decorate(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/Component;"
        )
    )
    private Component headapi$addHeadToChat(Component content) {
        if (this.headapi$sender != null && this.headapi$sender.getId() != null) {
            return ChatHeadHandler.appendHeadToMessage(this.headapi$sender.getId(), content);
        }
        return content;
    }
}
