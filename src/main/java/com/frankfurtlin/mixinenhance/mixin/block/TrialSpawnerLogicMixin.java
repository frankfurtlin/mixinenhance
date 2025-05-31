package com.frankfurtlin.mixinenhance.mixin.block;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.block.spawner.TrialSpawnerLogic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/5/30 23:51
 */
@Mixin(TrialSpawnerLogic.class)
public abstract class TrialSpawnerLogicMixin {
    @Shadow @Final private int cooldownLength;

    // 修改试炼刷怪笼的冷却时间，单位分钟
    @ModifyConstant(method = "<init>(Lnet/minecraft/block/spawner/TrialSpawnerLogic$TrialSpawner;Lnet/minecraft/block/spawner/EntityDetector;Lnet/minecraft/block/spawner/EntityDetector$Selector;)V", constant = @Constant(intValue = 36000))
    private static int changeTrialSpawnerCoolDown(int constant){
        return MixinEnhanceClient.getConfig().blockModuleConfig.trialSpawnerCoolDown * 60 * 20;
    }

    // 修改试炼刷怪笼的冷却时间，单位分钟
    @Inject(method = "getCooldownLength", at = @At("HEAD"), cancellable = true)
    public void getCooldownLength(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Math.min(MixinEnhanceClient.getConfig().blockModuleConfig.trialSpawnerCoolDown * 60 * 20, this.cooldownLength));
    }
}
