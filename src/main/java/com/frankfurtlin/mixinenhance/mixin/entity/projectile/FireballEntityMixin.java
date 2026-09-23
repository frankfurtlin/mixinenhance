package com.frankfurtlin.mixinenhance.mixin.entity.projectile;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/15 12:16
 */
@Mixin(LargeFireball.class)
public abstract class FireballEntityMixin {
    // 修改火球的伤害（恶魂）
    @ModifyConstant(method = "onHitEntity", constant = @Constant(floatValue = 6.0f))
    private float fireballDamage(float original) {
        return MixinEnhanceClient.getConfig().itemModuleConfig.fireballDamage;
    }
}
