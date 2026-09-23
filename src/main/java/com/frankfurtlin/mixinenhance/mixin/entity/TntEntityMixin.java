package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 7:37
 */
@Mixin(PrimedTnt.class)
public abstract class TntEntityMixin{

    @Shadow
    private float explosionPower;

    // TNT爆炸强度修改
    @Inject(method = "<init>*", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.explosionPower = MixinEnhanceClient.getConfig().itemModuleConfig.tntConfig.explodeRadius;
    }
}
