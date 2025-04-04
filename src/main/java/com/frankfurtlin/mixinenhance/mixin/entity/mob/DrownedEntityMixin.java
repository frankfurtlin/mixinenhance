package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.mob.DrownedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 12:43
 */
@Mixin(DrownedEntity.class)
public abstract class DrownedEntityMixin {
    // 修改溺尸生成时带有鹦鹉螺壳的概率
    @ModifyConstant(method = "initialize", constant = @Constant(floatValue = 0.03f))
    private float drownedSpawnWithShell(float constant){
        return MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.drownedSpawnWithShell;
    }

    // 修改溺尸在河流群系的生成概率
    @ModifyConstant(method = "canSpawn(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)Z", constant = @Constant(intValue = 15))
    private static int drownedSpawnInRiverFactor(int constant){
        return constant / MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.drownedSpawnFactor;
    }

    // 修改溺尸在海洋群系的生成概率
    @ModifyConstant(method = "canSpawn(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)Z", constant = @Constant(intValue = 40))
    private static int drownedSpawnInOceanFactor(int constant){
        return constant / MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.drownedSpawnFactor;
    }

    // 修改溺尸生成时带有工具的概率
    @ModifyConstant(method = "initEquipment", constant = @Constant(doubleValue = 0.9D))
    private double drownedSpawnWithTool(double constant){
        return 1 - MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.drownedSpawnWithTool;
    }

    // 根据难度系数修改溺尸的三叉戟伤害
    @ModifyConstant(method = "shootAt", constant = @Constant(floatValue = 1.6f))
    private float shootAt(float original) {
        int index = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.difficultyIndex;
        return original * index;
    }
}
