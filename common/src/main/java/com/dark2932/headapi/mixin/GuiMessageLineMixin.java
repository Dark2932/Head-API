package com.dark2932.headapi.mixin;

import com.dark2932.headapi.ChatHeads;
import com.dark2932.headapi.HeadData;
import com.dark2932.headapi.mixininterface.HeadRenderable;
import net.minecraft.client.GuiMessage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMessage.Line.class)
public abstract class GuiMessageLineMixin implements HeadRenderable {
    @Unique @NotNull
    public HeadData headapi$headData = HeadData.EMPTY;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void headapi$setHeadData(CallbackInfo ci) {
        headapi$headData = ChatHeads.getLineData();
        ChatHeads.setLineData(HeadData.EMPTY);
    }

    @Override @NotNull
    public HeadData headapi$getHeadData() {
        return headapi$headData;
    }

    @Override
    public void headapi$setHeadData(@NotNull HeadData headData) {
        headapi$headData = headData;
    }
}
