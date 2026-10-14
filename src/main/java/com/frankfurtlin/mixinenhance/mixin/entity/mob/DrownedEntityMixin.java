package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceConfig;
import net.minecraft.world.entity.monster.zombie.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 12:43
 */
@Mixin(Drowned.class)
public abstract class DrownedEntityMixin {
    // 修改溺尸在河流群系的生成概率
    @ModifyConstant(method = "checkDrownedSpawnRules(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z", constant = @Constant(intValue = 15), require = 0)
    private static int drownedSpawnInRiverFactor(int constant) {
        return constant / MixinEnhanceConfig.get().entityConfig.hostileMobConfig.drownedSpawnFactor;
    }

    // 修改溺尸在海洋群系的生成概率
    @ModifyConstant(method = "checkDrownedSpawnRules(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z", constant = @Constant(intValue = 40), require = 0)
    private static int drownedSpawnInOceanFactor(int constant) {
        return constant / MixinEnhanceConfig.get().entityConfig.hostileMobConfig.drownedSpawnFactor;
    }

    // 原版 0.9D 是「溺尸是否手持（三叉戟/钓鱼竿）」的门槛：原版掷出 > 0.9 才给（约 10%）。
    // 这里把门槛改成 1 - 武器系数，使「手持概率 = 武器系数」；三叉戟没有材质变体，
    // 因此溺尸只受概率与附魔影响（在袭击/生成时都适用）。
    @ModifyConstant(method = "populateDefaultEquipmentSlots", constant = @Constant(doubleValue = 0.9D), require = 0)
    private double mixinEnhance$weaponChance(double constant) {
        return 1.0D - MixinEnhanceConfig.get().entityConfig.equipmentConfig.weaponFactor;
    }
}
