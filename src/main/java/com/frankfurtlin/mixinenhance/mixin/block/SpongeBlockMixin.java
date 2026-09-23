package com.frankfurtlin.mixinenhance.mixin.block;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SpongeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpongeBlock.class)
public abstract class SpongeBlockMixin extends Block {
    @Shadow @Final private static Direction[] ALL_DIRECTIONS;

    public SpongeBlockMixin(Properties settings) {
        super(settings);
    }

    // 海绵最大吸附深度
    @ModifyConstant(method = "removeWaterBreadthFirstSearch", constant = @Constant(intValue = 6))
    private int changeMaxDepth(int constant){
        return MixinEnhanceClient.getConfig().itemModuleConfig.spongeConfig.maxDepth;
    }

    // 海绵最大吸附方块数
    @ModifyConstant(method = "removeWaterBreadthFirstSearch", constant = @Constant(intValue = 65))
    private int changeMaxIterations(int constant){
        return MixinEnhanceClient.getConfig().itemModuleConfig.spongeConfig.maxIterations;
    }

    // 海绵吸收岩浆
    @Inject(method = "tryAbsorbWater", at = @At("TAIL"))
    private void update(Level world, BlockPos pos, CallbackInfo ci) {
        if (MixinEnhanceClient.getConfig().itemModuleConfig.spongeConfig.canAbsorbLava) {
            if (absorbLava(world, pos)) {
                world.setBlock(pos, Blocks.WET_SPONGE.defaultBlockState(), Block.UPDATE_CLIENTS);
                world.playSound(null, pos, SoundEvents.SPONGE_ABSORB, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    @Unique
    private boolean absorbLava(Level world, BlockPos pos) {
        return BlockPos.breadthFirstTraversal(pos, MixinEnhanceClient.getConfig().itemModuleConfig.spongeConfig.maxDepth,
            MixinEnhanceClient.getConfig().itemModuleConfig.spongeConfig.maxIterations, (currentPos, queuer) -> {
            for (Direction direction : ALL_DIRECTIONS) {
                queuer.accept(currentPos.relative(direction));
            }
        }, currentPos -> {
            if (currentPos.equals(pos)) {
                return BlockPos.TraversalNodeStatus.ACCEPT;
            } else {
                BlockState blockState = world.getBlockState(currentPos);
                FluidState fluidState = world.getFluidState(currentPos);
                if (!fluidState.is(FluidTags.LAVA)) {
                    return BlockPos.TraversalNodeStatus.SKIP;
                } else {
                    if (blockState.getBlock() instanceof BucketPickup fluidDrainable && !fluidDrainable.pickupBlock(null, world, currentPos, blockState).isEmpty()) {
                        return BlockPos.TraversalNodeStatus.ACCEPT;
                    }

                    if (blockState.getBlock() instanceof LiquidBlock) {
                        world.setBlock(currentPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    } else {
                        if (!blockState.is(Blocks.KELP) && !blockState.is(Blocks.KELP_PLANT) && !blockState.is(Blocks.SEAGRASS) && !blockState.is(Blocks.TALL_SEAGRASS)) {
                            return BlockPos.TraversalNodeStatus.SKIP;
                        }

                        BlockEntity blockEntity = blockState.hasBlockEntity() ? world.getBlockEntity(currentPos) : null;
                        dropResources(blockState, world, currentPos, blockEntity);
                        world.setBlock(currentPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }

                    return BlockPos.TraversalNodeStatus.ACCEPT;
                }
            }
        }) > 1;
    }
}