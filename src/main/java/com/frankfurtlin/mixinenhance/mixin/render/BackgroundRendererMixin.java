package com.frankfurtlin.mixinenhance.mixin.render;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.FogModifier;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 11:20
 */
@Mixin(value = FogRenderer.class, priority = 1001)
public abstract class BackgroundRendererMixin {
    // 在水中/熔岩中时，不渲染雾
    // 1.21.11 中雾效系统重构为 FogRenderer + FogModifier，
    // 改为重定向 FogModifier.applyStartEndModifier，在应用后把雾的起止距离拉远至渲染距离
    @Redirect(method = "applyFog", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/render/fog/FogModifier;applyStartEndModifier(Lnet/minecraft/client/render/fog/FogData;Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/world/ClientWorld;FLnet/minecraft/client/render/RenderTickCounter;)V"))
    private void changeFogInLava(FogModifier fogModifier, FogData fogData, Camera camera, ClientWorld clientWorld,
                                 float viewDistance, RenderTickCounter renderTickCounter) {
        fogModifier.applyStartEndModifier(fogData, camera, clientWorld, viewDistance, renderTickCounter);
        if (MixinEnhanceClient.getConfig().entityModuleConfig.playerConfig.fluidVisible) {
            CameraSubmersionType submersionType = camera.getSubmersionType();
            if (submersionType == CameraSubmersionType.WATER || submersionType == CameraSubmersionType.LAVA) {
                fogData.environmentalStart = viewDistance * 0.75f;
                fogData.environmentalEnd = viewDistance;
                fogData.skyEnd = viewDistance;
                fogData.cloudEnd = viewDistance;
            }
        }
    }
}
