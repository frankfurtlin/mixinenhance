package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 15:14
 */
@Mixin(VindicatorEntity.class)
public abstract class VindicatorEntityMixin extends RaiderEntity{
    protected VindicatorEntityMixin(EntityType<? extends RaiderEntity> entityType, World world) {
        super(entityType, world);
    }

    // 根据难度系数修改卫道士的血量、攻击力
    @Inject(method = "<init>", at = @At("TAIL"))
    private void customHealthAndAttackDamage(EntityType<? extends VindicatorEntity> entityType, World world, CallbackInfo ci){
        int index = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.difficultyIndex;
        double health = 24.0 * index;
        double attack = 5.0 * index;
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.MAX_HEALTH)).setBaseValue(health);
        this.setHealth((float) health);
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE)).setBaseValue(attack);
    }

    // 卫道士武器升级（铁、钻石、下届合金剑）
    @Inject(method = "initEquipment", at = @At("TAIL"))
    public void initEquipment(Random random, LocalDifficulty localDifficulty, CallbackInfo ci) {
        if (this.getRaid() == null &&
            MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.enableVindicatorWeaponEnhancement) {
            int level = random.nextInt(3);
            if (level == 0) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
            } else if (level == 1) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_AXE));
            } else {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_AXE));
            }
        }
    }
}
