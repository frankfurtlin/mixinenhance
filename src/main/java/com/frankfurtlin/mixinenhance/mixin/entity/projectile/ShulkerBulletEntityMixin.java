package com.frankfurtlin.mixinenhance.mixin.entity.projectile;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 8:09
 */
@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletEntityMixin {
    // 根据攻击倍率修改潜影贝子弹伤害
    @ModifyConstant(method = "onHitEntity", constant = @Constant(floatValue = 4.0f))
    private float onEntityHit(float original) {
        return (float) (original * MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.attackMultiplier);
    }
}
