package com.frankfurtlin.mixinenhance.mixin;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantOffer.class)
public abstract class TradeOfferMixin {
    // 村民无限交易
    @Inject(method = "increaseUses", at = @At("HEAD"), cancellable = true)
    public void use(CallbackInfo ci) {
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.unLockTrade){
            ci.cancel();
        }
    }
}