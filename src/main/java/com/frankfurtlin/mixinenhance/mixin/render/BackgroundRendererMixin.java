package com.frankfurtlin.mixinenhance.mixin.render;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 11:20
 */
@Mixin(FogRenderer.class)
public abstract class BackgroundRendererMixin {
    // 在水中/熔岩中时，不渲染雾
    // 26.3 中雾效由 FogEnvironment 体系计算，改为重定向 FogEnvironment.setupFog，
    // 在水/熔岩环境下把雾的起止距离拉远到渲染距离
    @Redirect(method = "setupFog", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;setupFog(Lnet/minecraft/client/renderer/fog/FogData;Lnet/minecraft/client/Camera;Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/DeltaTracker;)V"))
    private void changeFogInFluid(FogEnvironment environment, FogData fogData, Camera camera, ClientLevel clientLevel,
                                  float renderDistance, DeltaTracker deltaTracker) {
        environment.setupFog(fogData, camera, clientLevel, renderDistance, deltaTracker);
        if (MixinEnhanceClient.getConfig().entityModuleConfig.playerConfig.fluidVisible
            && (environment instanceof WaterFogEnvironment || environment instanceof LavaFogEnvironment)) {
            fogData.environmentalStart = renderDistance * 0.75f;
            fogData.environmentalEnd = renderDistance;
            fogData.skyEnd = renderDistance;
            fogData.cloudEnd = renderDistance;
        }
    }
}
