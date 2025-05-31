package com.frankfurtlin.mixinenhance.mixin.block.vault;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.block.vault.VaultServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/5/31 11:57
 */
@Mixin(VaultServerData.class)
public abstract class VaultServerDataMixin {
    // 宝库无限兑换
    @Inject(method = "hasRewardedPlayer", at = @At("HEAD"), cancellable = true)
    private void hasRewardedPlayer(CallbackInfoReturnable<Boolean> cir) {
        if (MixinEnhanceClient.getConfig().blockModuleConfig.unLockVaultReward) {
            cir.setReturnValue(false);
        }
    }
}
