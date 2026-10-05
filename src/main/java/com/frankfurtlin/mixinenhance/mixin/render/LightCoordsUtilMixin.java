package com.frankfurtlin.mixinenhance.mixin.render;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import com.frankfurtlin.mixinenhance.client.DynamicLightHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 动态照明注入点：方块渲染的光照坐标计算最终会经过
 * LightCoordsUtil.getLightCoords(BrightnessGetter, BlockAndLightGetter, BlockState, BlockPos)，
 * 在返回的光照坐标上叠加动态光源贡献。
 */
@Mixin(LightCoordsUtil.class)
public class LightCoordsUtilMixin {
    @Inject(
        method = "getLightCoords(Lnet/minecraft/util/LightCoordsUtil$BrightnessGetter;Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void addDynamicLight(LightCoordsUtil.BrightnessGetter getter, BlockAndLightGetter level, BlockState state, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (!MixinEnhanceClient.getConfig().entityModuleConfig.playerConfig.enableDynamicLight) {
            return;
        }

        double dynamicLight = DynamicLightHandler.getDynamicLightLevel(pos);
        if (dynamicLight <= 0) {
            return;
        }

        int lightmap = cir.getReturnValue();
        int blockLevel = LightCoordsUtil.block(lightmap);
        int dynamicBlock = (int) dynamicLight;
        if (dynamicBlock > blockLevel) {
            cir.setReturnValue(LightCoordsUtil.withBlock(lightmap, dynamicBlock));
        }
    }
}
