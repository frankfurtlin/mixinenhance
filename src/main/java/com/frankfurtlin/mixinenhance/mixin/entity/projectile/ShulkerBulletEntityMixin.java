package com.frankfurtlin.mixinenhance.mixin.entity.projectile;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 8:09
 */
@Mixin(ShulkerBulletEntity.class)
public abstract class ShulkerBulletEntityMixin {
    // 根据难度系数修改潜影壳的子弹伤害
    @ModifyConstant(method = "onEntityHit", constant = @Constant(floatValue = 4.0f))
    private float onEntityHit(float original) {
        int index = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.difficultyIndex;
        return original * index;
    }
}
