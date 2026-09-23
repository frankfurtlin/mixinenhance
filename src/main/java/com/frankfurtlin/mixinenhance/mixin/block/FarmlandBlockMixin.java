package com.frankfurtlin.mixinenhance.mixin.block;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/6/21 9:42
 */
@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin extends Block  {
    public FarmlandBlockMixin(Properties settings) {
        super(settings);
    }

    // 耕地践踏不转化成泥土
    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    private void onLandedUpon(Level world, BlockState state, BlockPos pos, Entity entity, double fallDistance, CallbackInfo ci) {
        if (MixinEnhanceClient.getConfig().defaultModuleConfig.enableFarmlandNoLandToDirt) {
            super.fallOn(world, state, pos, entity, fallDistance);
            ci.cancel();
        }
    }
}
