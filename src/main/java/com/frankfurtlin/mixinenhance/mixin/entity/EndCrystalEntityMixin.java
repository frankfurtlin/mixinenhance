package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 7:37
 */
@Mixin(EndCrystal.class)
public abstract class EndCrystalEntityMixin {
    // 末地水晶爆炸强度修改
    @ModifyConstant(method = "hurtServer", constant = @Constant(floatValue = 6.0F))
    private float damage(float constant){
        return MixinEnhanceClient.getConfig().itemModuleConfig.endCrystalConfig.explodeRadius;
    }

}
