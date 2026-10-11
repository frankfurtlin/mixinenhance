package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import com.frankfurtlin.mixinenhance.util.ItemTagUtil;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
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

    // 凋零骷髅武器升级：从原版「剑」标签随机装备一把（自动包含后续新增的剑）
    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    public void initEquipment(RandomSource random, DifficultyInstance localDifficulty, CallbackInfo ci) {
        if (MixinEnhanceClient.getConfig().entityConfig.hostileMobConfig.enableWitherSkeletonWeaponEnhancement) {
            ItemStack sword = ItemTagUtil.randomStack(random, ItemTags.SWORDS);
            if (sword != null) {
                this.setItemSlot(EquipmentSlot.MAINHAND, sword);
            }
        }
    }
}
