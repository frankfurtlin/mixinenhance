package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 19:13
 */
@Mixin(Ghast.class)
public abstract class GhastEntityMixin extends Mob {
    @Shadow private int explosionPower;

    protected GhastEntityMixin(EntityType<? extends Mob> entityType, Level world) {
        super(entityType, world);
    }

    // 根据攻击倍率修改恶魂的火球爆炸威力
    @Inject(method = "<init>", at = @At("TAIL"))
    private void fireballStrengthFactor(EntityType<? extends Ghast> entityType,
                                        Level world, CallbackInfo ci){
        double attackMultiplier = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.attackMultiplier;
        this.explosionPower = (int) Math.max(1, this.explosionPower * attackMultiplier);
    }

    // 修改恶魂生成率
    // 26.3 中 canSpawn 更名为 checkGhastSpawnRules
    @ModifyConstant(method = "checkGhastSpawnRules", constant = @Constant(intValue = 20))
    private static int spawnRate(int constant){
        return 20 / MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.ghastSpawnFactor;
    }
}
