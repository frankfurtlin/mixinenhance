package com.frankfurtlin.mixinenhance.mixin.block;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
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
    @Shadow @Final private static Direction[] DIRECTIONS;

    public SpongeBlockMixin(Settings settings) {
        super(settings);
    }

    // 海绵最大吸附深度
    @ModifyConstant(method = "absorbWater", constant = @Constant(intValue = 6))
    private int changeMaxDepth(int constant){
        return MixinEnhanceClient.getConfig().itemModuleConfig.spongeConfig.maxDepth;
    }

    // 海绵最大吸附方块数
    @ModifyConstant(method = "absorbWater", constant = @Constant(intValue = 65))
    private int changeMaxIterations(int constant){
        return MixinEnhanceClient.getConfig().itemModuleConfig.spongeConfig.maxIterations;
    }

    // 海绵吸收岩浆
    @Inject(method = "update", at = @At("TAIL"))
    private void update(World world, BlockPos pos, CallbackInfo ci) {
        if (MixinEnhanceClient.getConfig().itemModuleConfig.spongeConfig.canAbsorbLava) {
            if (absorbLava(world, pos)) {
                world.setBlockState(pos, Blocks.WET_SPONGE.getDefaultState(), Block.NOTIFY_LISTENERS);
                world.playSound(null, pos, SoundEvents.BLOCK_SPONGE_ABSORB, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    @Unique
    private boolean absorbLava(World world, BlockPos pos) {
        return BlockPos.iterateRecursively(pos, MixinEnhanceClient.getConfig().itemModuleConfig.spongeConfig.maxDepth,
            MixinEnhanceClient.getConfig().itemModuleConfig.spongeConfig.maxIterations, (currentPos, queuer) -> {
            for (Direction direction : DIRECTIONS) {
                queuer.accept(currentPos.offset(direction));
            }
        }, currentPos -> {
            if (currentPos.equals(pos)) {
                return BlockPos.IterationState.ACCEPT;
            } else {
                BlockState blockState = world.getBlockState(currentPos);
                FluidState fluidState = world.getFluidState(currentPos);
                if (!fluidState.isIn(FluidTags.LAVA)) {
                    return BlockPos.IterationState.SKIP;
                } else {
                    if (blockState.getBlock() instanceof FluidDrainable fluidDrainable && !fluidDrainable.tryDrainFluid(null, world, currentPos, blockState).isEmpty()) {
                        return BlockPos.IterationState.ACCEPT;
                    }

                    if (blockState.getBlock() instanceof FluidBlock) {
                        world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    } else {
                        if (!blockState.isOf(Blocks.KELP) && !blockState.isOf(Blocks.KELP_PLANT) && !blockState.isOf(Blocks.SEAGRASS) && !blockState.isOf(Blocks.TALL_SEAGRASS)) {
                            return BlockPos.IterationState.SKIP;
                        }

                        BlockEntity blockEntity = blockState.hasBlockEntity() ? world.getBlockEntity(currentPos) : null;
                        dropStacks(blockState, world, currentPos, blockEntity);
                        world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    }

                    return BlockPos.IterationState.ACCEPT;
                }
            }
        }) > 1;
    }
}