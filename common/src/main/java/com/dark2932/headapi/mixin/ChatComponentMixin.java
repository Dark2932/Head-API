package com.dark2932.headapi.mixin;

import com.dark2932.headapi.ChatHeads;
import com.dark2932.headapi.HeadData;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @ModifyArg(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I",
            ordinal = 0
        ),
        index = 2
    )
    public int headapi$offsetText(Font font, FormattedCharSequence seq, int x, int y, int color) {
        return x + ChatHeads.headWidth();
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I",
            ordinal = 0
        )
    )
    public void headapi$setRenderContext(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, boolean focused, CallbackInfo ci,
            @Local(argsOnly = true) GuiGraphics gg, @Local GuiMessage.Line guiMessage) {
        HeadData headData = ChatHeads.getHeadData(guiMessage);
        ChatHeads.guiGraphics = gg;
        ChatHeads.renderHeadData = headData;
        ChatHeads.renderHeadOpacity = 1.0f;
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    public void headapi$clearRenderContext(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        ChatHeads.guiGraphics = null;
        ChatHeads.renderHeadData = HeadData.EMPTY;
    }
}
