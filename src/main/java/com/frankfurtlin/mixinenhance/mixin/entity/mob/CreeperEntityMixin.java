package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 17:36
 */
@Mixin(Creeper.class)
public abstract class CreeperEntityMixin extends Monster {
    protected CreeperEntityMixin(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
    }

    @Shadow
    private int explosionRadius;

    // 修改苦力怕爆炸半径
    @Inject(method = "<init>", at = @At("TAIL"))
    private void creeperExplodeRadius(CallbackInfo ci) {
        this.explosionRadius = MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.creeperExplodeRadius;
    }
}
