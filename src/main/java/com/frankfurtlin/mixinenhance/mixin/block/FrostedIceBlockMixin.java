package com.frankfurtlin.mixinenhance.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/5/15 23:01
 */
@Mixin(FrostedIceBlock.class)
public abstract class FrostedIceBlockMixin extends IceBlock  {
    public FrostedIceBlockMixin(Properties settings) {
        super(settings);
    }

    // 在地狱的时候霜冰化成熔岩
    @Redirect(method = "slightlyMelt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FrostedIceBlock;melt(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void increaseAge(FrostedIceBlock instance, BlockState blockState, Level world, BlockPos blockPos) {
        // Temporarily disabled due to API changes
        world.setBlockAndUpdate(blockPos, meltsInto());
        world.neighborChanged(blockPos, meltsInto().getBlock(), null);
    }

    // 在地狱的时候霜冰化成熔岩
    @Redirect(method = "neighborChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FrostedIceBlock;melt(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void neighborUpdate(FrostedIceBlock instance, BlockState blockState, Level world, BlockPos blockPos) {
        // Temporarily disabled due to API changes
        world.setBlockAndUpdate(blockPos, meltsInto());
        world.neighborChanged(blockPos, meltsInto().getBlock(), null);
    }
}
