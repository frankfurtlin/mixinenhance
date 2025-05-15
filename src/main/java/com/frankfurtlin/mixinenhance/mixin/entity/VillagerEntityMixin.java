package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin {
    // 每一等级村民解锁的交易选项数
    @ModifyConstant(method = "fillRecipes", constant = @Constant(intValue = 2))
    private int addMoreRecipeCount(int count){
        return MixinEnhanceClient.getConfig().defaultModuleConfig.tradeCount;
    }
}