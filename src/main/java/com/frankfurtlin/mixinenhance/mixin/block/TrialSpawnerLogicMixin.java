package com.frankfurtlin.mixinenhance.mixin.block;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/5/30 23:51
 */
@Mixin(TrialSpawner.class)
public abstract class TrialSpawnerLogicMixin {

    // 修改试炼刷怪笼的冷却时间，单位分钟
    // 1.21.11 将 cooldownLength 移入 FullConfig#targetCooldownLength，改为在返回值处修改
    @Inject(method = "getTargetCooldownLength", at = @At("RETURN"), cancellable = true)
    public void getCooldownLength(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Math.min(MixinEnhanceClient.getConfig().blockModuleConfig.trialSpawnerCoolDown * 60 * 20, cir.getReturnValue()));
    }
}
