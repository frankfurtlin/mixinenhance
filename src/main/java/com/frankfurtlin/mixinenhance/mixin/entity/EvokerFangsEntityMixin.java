package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.mob.EvokerFangsEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 9:06
 */
@Mixin(EvokerFangsEntity.class)
public abstract class EvokerFangsEntityMixin {
    // 根据难度系数修改幻魔者尖牙伤害修改
    @ModifyConstant(method = "damage(Lnet/minecraft/entity/LivingEntity;)V", constant = @Constant(floatValue = 6.0F))
    private float damage(float original) {
        int index = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.difficultyIndex;
        return original * index;
    }
}
