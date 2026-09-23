package com.frankfurtlin.mixinenhance.mixin.render;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 11:20
 */
@Mixin(ScreenEffectRenderer.class)
public abstract class InGameOverlayRendererMixin {
    // 移除火焰效果
    // 26.3 中 InGameOverlayRenderer 已重构为 ScreenEffectRenderer，
    // 火焰遮罩由私有的静态 submitFire 方法提交
    @Inject(method = "submitFire", at = @At(value = "HEAD"), cancellable = true)
    private static void removeFireOverlay(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                         TextureAtlasSprite sprite, CallbackInfo ci) {
        if (MixinEnhanceClient.getConfig().entityModuleConfig.playerConfig.noOverlay) {
            ci.cancel();
        }
    }
}
