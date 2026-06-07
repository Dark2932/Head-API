package com.dark2932.headapi.mixin;

import com.dark2932.headapi.ChatHeads;
import com.dark2932.headapi.HeadData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class FontStringRenderOutputMixin {
    @Shadow float x;
    @Shadow float y;
    @Shadow @Final private Matrix4f pose;
    @Shadow @Final private boolean dropShadow;

    @Unique
    private int headapi$charsRendered = 0;

    @Unique
    private float headapi$lastX;

    @Unique
    private float headapi$lastY;

    @Inject(method = "accept", at = @At("HEAD"))
    public void headapi$renderInlineHead(int index, Style style, int codepoint, CallbackInfoReturnable<Boolean> cir) {
        if (ChatHeads.renderHeadData == HeadData.EMPTY)
            return;

        if (ChatHeads.renderHeadData.endOfLine()) {
            headapi$lastX = x;
            headapi$lastY = y;
            return;
        }

        int renderIndex = Math.max(ChatHeads.renderHeadData.codePointIndex(), 0);

        if (headapi$charsRendered == renderIndex) {
            if (!dropShadow) {
                PoseStack poseStack = ChatHeads.guiGraphics.pose();
                poseStack.pushPose();
                poseStack.setIdentity();
                poseStack.mulPose(pose);

                ChatHeads.renderChatHead(ChatHeads.guiGraphics, (int) x, (int) y, ChatHeads.renderHeadData.uuid(), ChatHeads.renderHeadOpacity);

                poseStack.popPose();
            }

            x += ChatHeads.headWidth();
        }

        headapi$charsRendered++;
    }

    @Inject(method = "accept", at = @At("RETURN"))
    public void headapi$renderAtEnd(int index, Style style, int codepoint, CallbackInfoReturnable<Boolean> cir) {
        if (ChatHeads.renderHeadData == HeadData.EMPTY)
            return;

        if (ChatHeads.renderHeadData.endOfLine() && !dropShadow) {
            PoseStack poseStack = ChatHeads.guiGraphics.pose();
            poseStack.pushPose();
            poseStack.setIdentity();
            poseStack.mulPose(pose);

            ChatHeads.renderChatHead(ChatHeads.guiGraphics, (int) headapi$lastX + 2, (int) headapi$lastY, ChatHeads.renderHeadData.uuid(), ChatHeads.renderHeadOpacity);

            poseStack.popPose();
        }
    }
}
