package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 14:12
 */
@Mixin(WitherSkeleton.class)
public abstract class WitherSkeletonEntityMixin extends AbstractSkeleton {
    protected WitherSkeletonEntityMixin(EntityType<? extends AbstractSkeleton> entityType, Level world) {
        super(entityType, world);
    }

    // 凋零骷髅武器升级（石、铁、钻石、下届合金剑）
    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    public void initEquipment(RandomSource random, DifficultyInstance localDifficulty, CallbackInfo ci) {
        if (MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.enableWitherSkeletonWeaponEnhancement) {
            int level = random.nextInt(4);
            if (level == 0) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
            } else if (level == 1) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            } else if (level == 2) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
            }
        }
    }

}
