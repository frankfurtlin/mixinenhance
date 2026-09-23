package com.frankfurtlin.mixinenhance.mixin.entity.projectile;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/15 11:18
 */
@Mixin(SpectralArrow.class)
public abstract class SpectralArrowEntityMixin {
    // 光灵箭荧光持续时间修改
    @Redirect(method = "doPostHurtEffects",
        at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/arrow/SpectralArrow;duration:I", opcode = Opcodes.GETFIELD))
    private int spectralArrowDurationOnHit(SpectralArrow instance){
        return MixinEnhanceClient.getConfig().itemModuleConfig.spectralArrowDuration * 20;
    }

    // 光灵箭荧光持续时间修改
    // 1.21.11 将 writeCustomDataToNbt 改名为 writeCustomData
    @Redirect(method = "addAdditionalSaveData",
        at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/arrow/SpectralArrow;duration:I", opcode = Opcodes.GETFIELD))
    private int spectralArrowDurationToNBT(SpectralArrow instance){
        return MixinEnhanceClient.getConfig().itemModuleConfig.spectralArrowDuration * 20;
    }
}
