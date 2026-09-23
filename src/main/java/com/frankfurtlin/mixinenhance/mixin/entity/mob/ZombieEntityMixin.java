package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
            this.setCanPickUpLoot(random.nextFloat() < MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.pickupLootChance);
        }
    }

    // 修改僵尸生成时自带的武器概率，
    // 同时修改僵尸武器（铁锹、铁剑）->
    //   （铁锹、铁镐、铁剑、铁斧、铁锄）、
    //   （钻石锹、钻石镐、钻石剑、钻石斧、钻石锄）、
    //   （下届合金锹、下届合金镐、下届合金剑、下届合金斧、下届合金锄）
    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    public void initEquipment(RandomSource random, DifficultyInstance localDifficulty, CallbackInfo ci) {
        float f = random.nextFloat();
        float f2 = (float) MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.zombieSpawnWithTool;
        if (f < f2) {
            int i = random.nextInt(2);
            if (MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.enableZombieWeaponEnhancement) {
                i = random.nextInt(15);
            }
            if (i == 0) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            } else if (i == 1) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
            } else if (i == 2) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
            } else if (i == 3) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
            } else if (i == 4) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_HOE));
            } else if (i == 5) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
            } else if (i == 6) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SHOVEL));
            } else if (i == 7) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_PICKAXE));
            } else if (i == 8) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_AXE));
            } else if (i == 9) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_HOE));
            } else if (i == 10) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
            } else if (i == 11) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SHOVEL));
            } else if (i == 12) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_PICKAXE));
            } else if (i == 13) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_AXE));
            } else if (i == 14) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_HOE));
            }
        }

    }
}
