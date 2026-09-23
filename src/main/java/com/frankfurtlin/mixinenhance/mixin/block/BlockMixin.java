package com.frankfurtlin.mixinenhance.mixin.block;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.SoulSandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/6/22 11:02
 */
@Mixin(Block.class)
public abstract class BlockMixin {
    // 灵魂沙+蜂蜜块上移动不减速
    @Inject(method = "getSpeedFactor", at = @At("HEAD"), cancellable = true)
    private void changeBlockVelocityMultiplier(CallbackInfoReturnable<Float> cir) {
        Block block = (Block) (Object)this;
        if (block instanceof SoulSandBlock && MixinEnhanceClient.getConfig().defaultModuleConfig.noSlowOnSoulSand) {
            cir.setReturnValue(1.0f);
        }
        if (block instanceof HoneyBlock && MixinEnhanceClient.getConfig().defaultModuleConfig.noSlowOnHoneyBlock) {
            cir.setReturnValue(1.0f);
        }
    }
}
