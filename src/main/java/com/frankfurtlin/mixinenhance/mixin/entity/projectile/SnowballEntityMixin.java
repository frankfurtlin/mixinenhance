package com.frankfurtlin.mixinenhance.mixin.entity.projectile;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/15 20:30
 */
@Mixin(SnowballEntity.class)
public abstract class SnowballEntityMixin extends ThrownItemEntity{
    public SnowballEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public SnowballEntityMixin(EntityType<? extends ThrownItemEntity> type, double x, double y, double z, World world, ItemStack stack) {
        super(type, x, y, z, world, stack);
    }

    public SnowballEntityMixin(EntityType<? extends ThrownItemEntity> type, LivingEntity owner, World world, ItemStack stack) {
        super(type, owner, world, stack);
    }

    // 雪球伤害修改
    @ModifyArg(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;serverDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"), index = 1)
    private float modifyDamageValue(float amount) {
        return MixinEnhanceClient.getConfig().itemModuleConfig.snowballDamage;
    }
}
