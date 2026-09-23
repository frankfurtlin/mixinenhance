package com.frankfurtlin.mixinenhance.mixin.entity.projectile;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/15 12:16
 */
@Mixin(SmallFireball.class)
public abstract class SmallFireballEntityMixin {
    // 修改小火球的伤害（火焰弹、烈焰人）
    @ModifyConstant(method = "onHitEntity", constant = @Constant(floatValue = 5.0f))
    private float smallFireballDamage(float original) {
        return MixinEnhanceClient.getConfig().itemModuleConfig.smallFireballDamage;
    }
}
