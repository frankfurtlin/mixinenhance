package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Consumer;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 8:26
 */
@Mixin(SnowGolem.class)
public abstract class SnowGolemEntityMixin extends AbstractGolem {
    protected SnowGolemEntityMixin(EntityType<? extends AbstractGolem> entityType, Level world) {
        super(entityType, world);
    }

    // 修改雪人的雪球飞行参数（与难度无关）
    @ModifyArg(method = "performRangedAttack", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectile(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/world/entity/projectile/Projectile;"), index = 3)
    private Consumer<Snowball> modifyDamageValue(Consumer<Snowball> beforeSpawn) {
        return entity -> {
            double d = entity.getX() - this.getX();
            double e = this.getEyeY() - 1.1F;
            double f = entity.getZ() - this.getZ();
            double g = Math.sqrt(d * d + f * f) * 0.2F;

            entity.shoot(d, e + g - entity.getY(), f, 1.6F, 12.0F);
        };
    }
}
