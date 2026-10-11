package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import com.frankfurtlin.mixinenhance.util.ItemTagUtil;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 15:14
 */
@Mixin(Vindicator.class)
public abstract class VindicatorEntityMixin extends Raider {
    protected VindicatorEntityMixin(EntityType<? extends Raider> entityType, Level world) {
        super(entityType, world);
    }

    // 卫道士武器升级：从原版「斧」标签随机装备一把（自动包含后续新增的斧头，如铜斧）
    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    public void initEquipment(RandomSource random, DifficultyInstance localDifficulty, CallbackInfo ci) {
        if (this.getCurrentRaid() == null &&
            MixinEnhanceClient.getConfig().entityConfig.hostileMobConfig.enableVindicatorWeaponEnhancement) {
            ItemStack axe = ItemTagUtil.randomStack(random, ItemTags.AXES);
            if (axe != null) {
                this.setItemSlot(EquipmentSlot.MAINHAND, axe);
            }
        }
    }
}
