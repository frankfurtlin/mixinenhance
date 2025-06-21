package com.frankfurtlin.mixinenhance.mixin.block;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    public FarmlandBlockMixin(Settings settings) {
        super(settings);
    }

    // 耕地践踏不转化成泥土
    @Inject(method = "onLandedUpon", at = @At("HEAD"), cancellable = true)
    private void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, double fallDistance, CallbackInfo ci) {
        if (MixinEnhanceClient.getConfig().defaultModuleConfig.enableFarmlandNoLandToDirt) {
            super.onLandedUpon(world, state, pos, entity, fallDistance);
            ci.cancel();
        }
    }
}
