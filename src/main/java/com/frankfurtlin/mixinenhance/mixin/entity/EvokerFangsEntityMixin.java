package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.projectile.EvokerFangs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 9:06
 */
@Mixin(EvokerFangs.class)
public abstract class EvokerFangsEntityMixin {
    // 根据攻击倍率修改幻魔者尖牙伤害
    @ModifyConstant(method = "dealDamageTo(Lnet/minecraft/world/entity/LivingEntity;)V", constant = @Constant(floatValue = 6.0F))
    private float damage(float original) {
        return (float) (original * MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.attackMultiplier);
    }
}
