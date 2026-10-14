package com.frankfurtlin.mixinenhance.mixin.entity.projectile;

import com.frankfurtlin.mixinenhance.MixinEnhanceConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
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
public abstract class SnowballEntityMixin extends ThrowableItemProjectile {
    // 只保留一个构造器：mixin 类声明多个构造器时，Mixin 需要「挑一个」并因此告警
    // （多个构造器只是为了调用 super，本身不会被使用）。写法与其它的 mixin 保持一致。
    public SnowballEntityMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    // 雪球伤害修改
    @ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"), index = 1)
    private float modifyDamageValue(float amount) {
        return MixinEnhanceConfig.get().itemConfig.projectileDamageConfig.snowballDamage;
    }
}
