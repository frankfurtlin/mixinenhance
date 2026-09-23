package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.monster.illager.Illusioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 14:43
 */
@Mixin(Illusioner.class)
public abstract class IllusionerEntityMixin {
    // 根据攻击倍率修改幻术师的弓箭伤害
    @ModifyConstant(method = "performRangedAttack", constant = @Constant(floatValue = 1.6f))
    private float shootAt(float original) {
        return (float) (original * MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.attackMultiplier);
    }
}
