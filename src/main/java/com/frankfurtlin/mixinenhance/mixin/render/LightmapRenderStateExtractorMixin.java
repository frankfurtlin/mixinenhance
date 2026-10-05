package com.frankfurtlin.mixinenhance.mixin.render;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 亮度开关（Gamma Utils 替代）：开启时强制光照贴图全亮（夜视般效果）。
 *
 * 26.3 里 gamma 选项（Options.gamma）被限制在 [0,1]，且渲染亮度公式为
 * brightness = max(0, gamma - 夜晚黑暗程度)，因此即使 gamma=1.0 夜晚也不够亮。
 * 这里直接在光照提取器 extract 末尾，把 brightness 置为 1.0、darknessEffectScale 置为 0.0，
 * 实现真正的"全亮"。
 */
@Mixin(LightmapRenderStateExtractor.class)
public class LightmapRenderStateExtractorMixin {
    @Inject(method = "extract", at = @At("RETURN"))
    private void forceFullBright(LightmapRenderState state, float partialTicks, CallbackInfo ci) {
        if (MixinEnhanceClient.getConfig().entityModuleConfig.playerConfig.enableGamma) {
            // 天空光拉满（夜晚变暗的主因）、整体亮度最大、无夜晚黑暗
            state.skyFactor = 1.0f;
            state.brightness = 1.0f;
            state.darknessEffectScale = 0.0f;
            // 直接复用原版夜视机制：夜视强度 100% + 白色夜视色，实现真正的"全亮"
            state.nightVisionEffectIntensity = 1.0f;
            state.nightVisionColor = LightmapRenderStateExtractor.WHITE;
        }
    }
}
