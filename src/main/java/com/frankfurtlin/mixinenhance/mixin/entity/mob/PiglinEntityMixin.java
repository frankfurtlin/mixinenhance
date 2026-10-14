package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceConfig;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 17:36
 */
@Mixin(Piglin.class)
public abstract class PiglinEntityMixin {
    // 猪灵生成时自带金质盔甲的概率 = 盔甲系数（原版 0.1F）
    // 武器（金剑/弩）由公共钩子按武器系数处理；盔甲材质保持原版金质
    @ModifyConstant(method = "maybeWearArmor", constant = @Constant(floatValue = 0.1f), require = 0)
    private float mixinEnhance$armorChance(float constant) {
        return (float) MixinEnhanceConfig.get().entityConfig.equipmentConfig.armorFactor;
    }
}
