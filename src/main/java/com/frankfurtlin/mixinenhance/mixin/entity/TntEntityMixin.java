package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.TntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 7:37
 */
@Mixin(TntEntity.class)
public abstract class TntEntityMixin{

    @Shadow
    private float explosionPower;

    // TNT爆炸强度修改
    @Inject(method = "<init>*", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.explosionPower = new Random().nextInt(MixinEnhanceClient.getConfig().itemModuleConfig.tntConfig.minExplodeRadius,
            MixinEnhanceClient.getConfig().itemModuleConfig.tntConfig.maxExplodeRadius);
    }
}
