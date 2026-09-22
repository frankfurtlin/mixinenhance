package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 19:13
 */
@Mixin(GhastEntity.class)
public abstract class GhastEntityMixin extends MobEntity {
    @Shadow private int fireballStrength;

    protected GhastEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    // 根据难度系数修改恶魂的血量
    @Inject(method = "<init>", at = @At("TAIL"))
    private void customHealth(EntityType<? extends GhastEntity> entityType, World world, CallbackInfo ci) {
        int index = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.difficultyIndex;
        double health = 10.0 * index;
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.MAX_HEALTH)).setBaseValue(health);
        this.setHealth((float) health);
    }

    // 根据难度系数修改恶魂的火球爆炸威力
    @Inject(method = "<init>", at = @At("TAIL"))
    private void fireballStrengthFactor(EntityType<? extends GhastEntity> entityType,
                                        World world, CallbackInfo ci){
        fireballStrength = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.difficultyIndex;
    }

    // 修改恶魂生成率
    @ModifyConstant(method = "canSpawn", constant = @Constant(intValue = 20))
    private static int spawnRate(int constant){
        return 20 / MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.ghastSpawnFactor;
    }
}
