package com.frankfurtlin.mixinenhance.mixin.block;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/5/31 13:33
 */
@Mixin(TrialSpawnerBlock.class)
public abstract class TrialSpawnerBlockMixin {
    // 降低硬度便于精准采集挖掘
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static BlockBehaviour.Properties modifyStrength(BlockBehaviour.Properties settings) {
        if (MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableTrialSpawnerDropWithSilkTouch) {
            return settings.strength(5);
        }
        return settings.strength(50);
    }
}
