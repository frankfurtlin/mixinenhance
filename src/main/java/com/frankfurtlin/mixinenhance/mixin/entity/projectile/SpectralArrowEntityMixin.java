package com.frankfurtlin.mixinenhance.mixin.entity.projectile;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/15 11:18
 */
@Mixin(SpectralArrowEntity.class)
public abstract class SpectralArrowEntityMixin {
    // 光灵箭荧光持续时间修改
    @Redirect(method = "onHit",
        at = @At(value = "FIELD", target = "Lnet/minecraft/entity/projectile/SpectralArrowEntity;duration:I", opcode = Opcodes.GETFIELD))
    private int spectralArrowDurationOnHit(SpectralArrowEntity instance){
        return MixinEnhanceClient.getConfig().itemModuleConfig.spectralArrowDuration * 20;
    }

    // 光灵箭荧光持续时间修改
    @Redirect(method = "writeCustomDataToNbt",
        at = @At(value = "FIELD", target = "Lnet/minecraft/entity/projectile/SpectralArrowEntity;duration:I", opcode = Opcodes.GETFIELD))
    private int spectralArrowDurationToNBT(SpectralArrowEntity instance){
        return MixinEnhanceClient.getConfig().itemModuleConfig.spectralArrowDuration * 20;
    }
}
