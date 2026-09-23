package com.frankfurtlin.mixinenhance.mixin.entity.projectile;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/15 14:14
 */
@Mixin(ThrownExperienceBottle.class)
public abstract class ExperienceBottleEntityMixin {
    // 经验瓶产出经验修改
    @ModifyConstant(method = "onHit", constant = @Constant(intValue = 5))
    private int experienceBottleValue(int constant){
        return MixinEnhanceClient.getConfig().itemModuleConfig.experienceBottleValue;
    }
}
