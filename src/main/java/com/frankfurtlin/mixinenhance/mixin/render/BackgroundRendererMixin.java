package com.frankfurtlin.mixinenhance.mixin.render;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BackgroundRenderer.class, priority = 1001)
public abstract class BackgroundRendererMixin {
    // 在水中/熔岩中时，不渲染雾
    @Inject(method = "applyFog", at = @At(value = "HEAD"), cancellable = true)
    private static void changeForInLava(Camera camera, BackgroundRenderer.FogType fogType, Vector4f color,
                                        float viewDistance, boolean thickenFog, float tickProgress,
                                        CallbackInfoReturnable<Fog> cir) {
        if (MixinEnhanceClient.getConfig().entityModuleConfig.playerConfig.fluidVisible) {
            cir.setReturnValue(Fog.DUMMY);
        }
    }
}