package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 14:12
 */
@Mixin(WitherSkeletonEntity.class)
public abstract class WitherSkeletonEntityMixin extends AbstractSkeletonEntity {
    protected WitherSkeletonEntityMixin(EntityType<? extends AbstractSkeletonEntity> entityType, World world) {
        super(entityType, world);
    }

    // 凋零骷髅武器升级（石、铁、钻石、下届合金剑）
    @Inject(method = "initEquipment", at = @At("TAIL"))
    public void initEquipment(Random random, LocalDifficulty localDifficulty, CallbackInfo ci) {
        if (MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.enableWitherSkeletonWeaponEnhancement) {
            int level = random.nextInt(4);
            if (level == 0) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
            } else if (level == 1) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            } else if (level == 2) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
            } else {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
            }
        }
    }

    // 根据难度系数修改凋零骷髅的攻击力
    @ModifyConstant(method = "initialize", constant = @Constant(doubleValue = 4.0f))
    private double shootAt(double original) {
        int index = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.difficultyIndex;
        return original * index;
    }
}
