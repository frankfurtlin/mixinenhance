package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 15:46
 */
@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends HostileEntity {
    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    // 修改僵尸拾取物品的概率
    @Inject(method = "initialize", at = @At("TAIL"))
    private void modifyPickupLootChance(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        if (spawnReason != SpawnReason.CONVERSION) {
            this.setCanPickUpLoot(random.nextFloat() < MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.pickupLootChance);
        }
    }

    // 根据难度系数修改僵尸的血量、攻击力
    @Inject(method = "<init>*", at = @At("TAIL"))
    private void customHealthAndAttackDamage(EntityType<? extends ZombieEntity> entityType, World world, CallbackInfo ci) {
        int index = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.difficultyIndex;
        double health = 20.0 * index;
        double attack = 3.0 * index;
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.MAX_HEALTH)).setBaseValue(health);
        this.setHealth((float) health);
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE)).setBaseValue(attack);
    }

    // 修改僵尸生成时自带的武器概率，
    // 同时修改僵尸武器（铁锹、铁剑）->
    //   （铁锹、铁镐、铁剑、铁斧、铁锄）、
    //   （钻石锹、钻石镐、钻石剑、钻石斧、钻石锄）、
    //   （下届合金锹、下届合金镐、下届合金剑、下届合金斧、下届合金锄）
    @Inject(method = "initEquipment", at = @At("TAIL"))
    public void initEquipment(Random random, LocalDifficulty localDifficulty, CallbackInfo ci) {
        float f = random.nextFloat();
        float f2 = (float) MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.zombieSpawnWithTool;
        if (f < f2) {
            int i = random.nextInt(2);
            if (MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.enableZombieWeaponEnhancement) {
                i = random.nextInt(15);
            }
            if (i == 0) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            } else if (i == 1) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
            } else if (i == 2) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
            } else if (i == 3) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
            } else if (i == 4) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_HOE));
            } else if (i == 5) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
            } else if (i == 6) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SHOVEL));
            } else if (i == 7) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_PICKAXE));
            } else if (i == 8) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_AXE));
            } else if (i == 9) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_HOE));
            } else if (i == 10) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
            } else if (i == 11) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SHOVEL));
            } else if (i == 12) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_PICKAXE));
            } else if (i == 13) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_AXE));
            } else if (i == 14) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_HOE));
            }
        }

    }
}
