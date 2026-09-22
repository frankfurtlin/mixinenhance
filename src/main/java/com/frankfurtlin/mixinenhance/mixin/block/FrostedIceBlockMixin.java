package com.frankfurtlin.mixinenhance.mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FrostedIceBlock;
import net.minecraft.block.IceBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    public FrostedIceBlockMixin(Settings settings) {
        super(settings);
    }

    // 在地狱的时候霜冰化成熔岩
    @Redirect(method = "increaseAge", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FrostedIceBlock;melt(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
    private void increaseAge(FrostedIceBlock instance, BlockState blockState, World world, BlockPos blockPos) {
        // Temporarily disabled due to API changes
        world.setBlockState(blockPos, getMeltedState());
        world.updateNeighbor(blockPos, getMeltedState().getBlock(), null);
    }

    // 在地狱的时候霜冰化成熔岩
    @Redirect(method = "neighborUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FrostedIceBlock;melt(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
    private void neighborUpdate(FrostedIceBlock instance, BlockState blockState, World world, BlockPos blockPos) {
        // Temporarily disabled due to API changes
        world.setBlockState(blockPos, getMeltedState());
        world.updateNeighbor(blockPos, getMeltedState().getBlock(), null);
    }
}
