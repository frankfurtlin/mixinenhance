package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 14:05
 */
@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonEntityMixin {
    // 根据攻击倍率修改骷髅的弓箭伤害
    @ModifyConstant(method = "performRangedAttack", constant = @Constant(floatValue = 1.6f))
    private float shootAt(float original) {
        return (float) (original * MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.attackMultiplier);
    }
}
