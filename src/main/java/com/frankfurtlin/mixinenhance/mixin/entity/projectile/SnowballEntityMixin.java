package com.frankfurtlin.mixinenhance.mixin.entity.projectile;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/15 20:30
 */
@Mixin(Snowball.class)
public abstract class SnowballEntityMixin extends ThrowableItemProjectile{
    public SnowballEntityMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public SnowballEntityMixin(EntityType<? extends ThrowableItemProjectile> type, double x, double y, double z, Level world, ItemStack stack) {
        super(type, x, y, z, world, stack);
    }

    public SnowballEntityMixin(EntityType<? extends ThrowableItemProjectile> type, LivingEntity owner, Level world, ItemStack stack) {
        super(type, owner, world, stack);
    }

    // 雪球伤害修改
    @ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"), index = 1)
    private float modifyDamageValue(float amount) {
        return MixinEnhanceClient.getConfig().itemModuleConfig.snowballDamage;
    }
}
