package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * 僵尸系（僵尸/尸壳/僵尸村民等）：
 * 自带武器的概率改为由「武器系数」决定（原版固定 5%），武器材质则由公共钩子统一处理。
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 15:46
 */
@Mixin(Zombie.class)
public abstract class ZombieEntityMixin extends Monster {
    protected ZombieEntityMixin(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
    }

    // 修改僵尸拾取物品的概率
    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void modifyPickupLootChance(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, SpawnGroupData entityData, CallbackInfoReturnable<SpawnGroupData> cir) {
        if (spawnReason != EntitySpawnReason.CONVERSION) {
            this.setCanPickUpLoot(random.nextFloat() < MixinEnhanceClient.getConfig().entityConfig.hostileMobConfig.pickupLootChance);
        }
    }

    // 僵尸在原版是「按概率带武器」（0.05F），所以这里把概率直接换成武器系数：
    // 系数 1 必带、0 不带。武器材质由公共钩子处理。
    @ModifyConstant(method = "populateDefaultEquipmentSlots", constant = @Constant(floatValue = 0.05F), require = 0)
    private float mixinEnhance$weaponChance(float chance) {
        return (float) MixinEnhanceClient.getConfig().entityConfig.equipmentConfig.weaponFactor;
    }
}
