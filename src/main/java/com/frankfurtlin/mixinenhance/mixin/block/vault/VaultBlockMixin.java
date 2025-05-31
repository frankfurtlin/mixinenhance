package com.frankfurtlin.mixinenhance.mixin.block.vault;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.VaultBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/5/31 13:33
 */
@Mixin(VaultBlock.class)
public abstract class VaultBlockMixin {
    // 降低硬度便于精准采集挖掘
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static AbstractBlock.Settings modifyStrength(AbstractBlock.Settings settings) {
        if (MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableVaultDropWithSilkTouch) {
            return settings.strength(5);
        }
        return settings.strength(50);
    }
}
