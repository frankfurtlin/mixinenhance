package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import com.frankfurtlin.mixinenhance.util.ItemTagUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
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

    // 修改僵尸生成时自带的武器概率；
    // 开启武器增强时，从原版「所有工具」标签（锹/镐/剑/斧/锄/矛）随机装备一件，
    // 从而自动包含后续新增的工具类型，无需改代码。
    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    public void initEquipment(RandomSource random, DifficultyInstance localDifficulty, CallbackInfo ci) {
        float f = random.nextFloat();
        float f2 = (float) MixinEnhanceClient.getConfig().entityConfig.hostileMobConfig.zombieSpawnWithTool;
        if (f < f2) {
            if (MixinEnhanceClient.getConfig().entityConfig.hostileMobConfig.enableZombieWeaponEnhancement) {
                ItemStack tool = ItemTagUtil.randomStack(random,
                    ItemTags.SHOVELS, ItemTags.PICKAXES, ItemTags.SWORDS,
                    ItemTags.AXES, ItemTags.HOES, ItemTags.SPEARS);
                if (tool != null) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, tool);
                }
            } else {
                // 原版：铁剑 / 铁锹 二选一
                this.setItemSlot(EquipmentSlot.MAINHAND,
                    new ItemStack(random.nextBoolean() ? Items.IRON_SWORD : Items.IRON_SHOVEL));
            }
        }
    }
}
