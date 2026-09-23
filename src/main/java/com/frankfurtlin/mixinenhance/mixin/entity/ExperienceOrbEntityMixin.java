package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/16 13:58
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbEntityMixin {
    // 玩家吸收经验无冷却
    @ModifyConstant(method = "playerTouch", constant = @Constant(intValue = 2))
    private int enablePlayerExpPickUpNoDelay(int original){
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.enablePlayerExpPickUpNoDelay){
            return 0;
        }
        return original;
    }
}
