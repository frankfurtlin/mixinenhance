package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/21 15:27
 */
@Mixin(PhantomEntity.class)
public abstract class PhantomEntityMixin extends FlyingEntity {
    @Shadow public abstract int getPhantomSize();

    protected PhantomEntityMixin(EntityType<? extends FlyingEntity> entityType, World world) {
        super(entityType, world);
    }

    // 根据难度系数修改幻翼的血量
    @Inject(method = "<init>", at = @At("TAIL"))
    private void customHealth(EntityType<? extends PhantomEntity> entityType, World world, CallbackInfo ci){
        int index = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.difficultyIndex;
        double health = 20.0 * index;
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.MAX_HEALTH)).setBaseValue(health);
        this.setHealth((float) health);
    }

    // 根据难度系数修改幻翼的攻击力
    @Inject(method = "onSizeChanged", cancellable = true, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/entity/mob/PhantomEntity;getAttributeInstance(Lnet/minecraft/registry/entry/RegistryEntry;)Lnet/minecraft/entity/attribute/EntityAttributeInstance;"))
    private void customAttackDamage(CallbackInfo ci){
        int index = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.difficultyIndex;
        double attack = (6 + this.getPhantomSize()) * index;
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE)).setBaseValue(attack);
        ci.cancel();
    }
}
