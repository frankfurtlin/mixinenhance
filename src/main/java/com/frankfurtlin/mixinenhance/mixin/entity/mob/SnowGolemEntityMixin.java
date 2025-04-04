package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 8:26
 */
@Mixin(SnowGolemEntity.class)
public abstract class SnowGolemEntityMixin extends GolemEntity {
    protected SnowGolemEntityMixin(EntityType<? extends GolemEntity> entityType, World world) {
        super(entityType, world);
    }

    // 根据难度系数修改雪傀儡的血量
    @Inject(method = "<init>", at = @At("TAIL"))
    private void customHealthAndAttackDamage(EntityType<? extends GolemEntity> entityType, World world, CallbackInfo ci){
        int index = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.difficultyIndex;
        double health = 4.0 * index;
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.MAX_HEALTH)).setBaseValue(health);
        this.setHealth((float) health);
    }

    // 根据难度系数修改雪人的雪球伤害
    @ModifyArg(method = "shootAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;spawn(Lnet/minecraft/entity/projectile/ProjectileEntity;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/entity/projectile/ProjectileEntity;"), index = 3)
    private Consumer<SnowballEntity> modifyDamageValue(Consumer<SnowballEntity> beforeSpawn) {
        return entity -> {
            double d = entity.getX() - this.getX();
            double e = this.getEyeY() - 1.1F;
            double f = entity.getZ() - this.getZ();
            double g = Math.sqrt(d * d + f * f) * 0.2F;

            entity.setVelocity(d, e + g - entity.getY(), f, 1.6F, 12.0F);
        };
    }
}
