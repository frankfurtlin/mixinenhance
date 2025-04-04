package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.Random;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 7:37
 */
@Mixin(EndCrystalEntity.class)
public abstract class EndCrystalEntityMixin {
    // 末地水晶爆炸强度修改
    @ModifyConstant(method = "damage", constant = @Constant(floatValue = 6.0F))
    private float damage(float constant){
        int value = new Random().nextInt(MixinEnhanceClient.getConfig().itemModuleConfig.endCrystalConfig.minExplodeRadius,
            MixinEnhanceClient.getConfig().itemModuleConfig.endCrystalConfig.maxExplodeRadius);
        return (float)value;
    }

}
