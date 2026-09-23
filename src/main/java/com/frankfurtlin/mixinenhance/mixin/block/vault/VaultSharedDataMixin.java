package com.frankfurtlin.mixinenhance.mixin.block.vault;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/5/31 11:57
 */
@Mixin(VaultSharedData.class)
public abstract class VaultSharedDataMixin {
    // 宝库无限兑换
    @Inject(method = "hasConnectedPlayers", at = @At("HEAD"), cancellable = true)
    private void hasConnectedPlayers(CallbackInfoReturnable<Boolean> cir) {
        if (MixinEnhanceClient.getConfig().blockModuleConfig.unLockVaultReward) {
            cir.setReturnValue(true);
        }
    }
}
