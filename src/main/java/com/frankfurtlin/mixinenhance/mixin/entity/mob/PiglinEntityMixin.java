package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 17:36
 */
@Mixin(Piglin.class)
public abstract class PiglinEntityMixin {
    // 猪灵在生成时带有金质盔甲的概率
    @ModifyConstant(method = "maybeWearArmor", constant = @Constant(floatValue = 0.1f))
    private float piglinSpawnWithArmor(float constant){
        return MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.piglinSpawnWithArmor;
    }
}
